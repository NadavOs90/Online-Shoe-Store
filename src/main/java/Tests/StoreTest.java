package Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.BuyResult;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;

public class StoreTest {

	@Before
	public void setUp() throws Exception {
		Store.getInstance().load(new ShoeStorageInfo[0]);
	}

	@Test
	public void testLoad() {
		Store.getInstance().load(new ShoeStorageInfo[]{new ShoeStorageInfo("shoe", 1)});
		assertEquals(Store.getInstance().take("naal", false), BuyResult.NOT_IN_STOCK);
		assertEquals(Store.getInstance().take("naal", true), BuyResult.NOT_ON_DISCOUNT);
		Store.getInstance().load(new ShoeStorageInfo[]{new ShoeStorageInfo("naal", 1)});
		assertNotNull(Store.getInstance().take("naal", false));
	}

	@Test
	public void testTake() {
		assertEquals(Store.getInstance().take("naal", false), BuyResult.NOT_IN_STOCK);
		Store.getInstance().load(new ShoeStorageInfo[]{new ShoeStorageInfo("naal", 1)});
		assertEquals(Store.getInstance().take("naal", false), BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAdd() {
		Store.getInstance().add("naal", 2);
		assertNotNull(Store.getInstance().take("naal", false));
		assertEquals(Store.getInstance().take("naal", false), BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAddDiscount() {
		Store.getInstance().add("naal", 2);
		Store.getInstance().addDiscount("naal", 1);
		assertEquals(Store.getInstance().take("naal", false), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("naal", false), BuyResult.REGULAR_PRICE);
	}

}

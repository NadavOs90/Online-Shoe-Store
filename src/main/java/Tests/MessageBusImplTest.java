package Tests;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.SellingService;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class MessageBusImplTest {
	private MicroService m;
	private TickBroadcast msg;

	@Before
	public void setUp() throws Exception {
		m=new SellingService ("Me", new CountDownLatch(0));
		msg=new TickBroadcast (0);
	}

	@Test
	public void testRegister() {
		assertEquals (true, MessageBusImpl.getInstance().done());
		MessageBusImpl.getInstance().register(m);
		assertEquals (false, MessageBusImpl.getInstance().done());
		MessageBusImpl.getInstance().unregister(m);
	}

	@Test
	public void testUnregister() {
		MessageBusImpl.getInstance().register(m);
		assertEquals (MessageBusImpl.getInstance().done(), false);
		MessageBusImpl.getInstance().unregister(m);
		assertEquals (MessageBusImpl.getInstance().done(), true);
	}

	@Test
	public void testAwaitMessage() throws InterruptedException {
		MessageBusImpl.getInstance().register(m);
		MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class, m);
		MessageBusImpl.getInstance().sendBroadcast(msg);
		assertSame(MessageBusImpl.getInstance().awaitMessage(m), msg);
		MessageBusImpl.getInstance().unregister(m);
	}

	@Test
	public void testDone() {
		assertEquals (MessageBusImpl.getInstance().done(), true);
		MessageBusImpl.getInstance().register(m);
		assertEquals (MessageBusImpl.getInstance().done(), false);
		MessageBusImpl.getInstance().unregister(m);
	}

}

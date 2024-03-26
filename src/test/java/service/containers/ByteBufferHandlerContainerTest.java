package service.containers;

import exceptions.HandleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import service.IdentifiableMessageHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class ByteBufferHandlerContainerTest {
    private static AbstractHandlerContainer<String, ByteBuffer> container;

    @BeforeAll
    static void init() {
        container = new ByteBufferHandlerContainer<>();
    }

    @AfterEach
    void tearDown() {
        container.removeAll();
    }

    @Test
    void getId() {
        IdentifiableMessageHandler<String, ByteBuffer> handler = new IdentifiableMessageHandler<>("print") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println(message);
            }
        };
        String expectedId = handler.getIdentifier();
        String actualId = container.getId(handler);
        assertEquals(expectedId, actualId);
    }

    @Test
    void invokeAll() {
        ByteBuffer message = ByteBuffer.allocate(3);
        message.put((byte) 1);
        message.put((byte) 2);
        message.put((byte) 3);
        List<ByteBuffer> list1 = new ArrayList<>();
        List<ByteBuffer> list2 = new ArrayList<>();
        container.addNew(new IdentifiableMessageHandler<>("toList1") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                list1.add(message);
            }
        });
        container.addNew(new IdentifiableMessageHandler<>("toList2") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                list2.add(message);
            }
        });
        try {
            container.invokeAll(message);
        } catch (HandleException e) {
            e.printStackTrace();
        }
        assertTrue(!list1.isEmpty() && !list2.isEmpty() && list1.size() == list2.size() && list1.containsAll(list2) && list2.containsAll(list1));
    }

    @Test
    void invokeAllHandlersWillNotBeInvoke() {
        ByteBuffer message = ByteBuffer.wrap(new byte[]{1, 2, 3});
        List<ByteBuffer> list1 = new ArrayList<>();
        List<ByteBuffer> list2 = new ArrayList<>();
        container.addNew(new IdentifiableMessageHandler<>("toList1") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                list1.add(message);
            }
        });
        container.addNew(new IdentifiableMessageHandler<>("toList2") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                list2.add(message);
            }
        });
        try {
            container.invokeAll(message);
        } catch (HandleException e) {
            e.printStackTrace();
        }
        assertTrue(list1.isEmpty() && list2.isEmpty());
    }

    @Test
    void invokeAllThrowsNPE() {
        ByteBuffer message = ByteBuffer.allocate(3);
        message.put((byte) 1);
        message.put((byte) 2);
        message.put((byte) 3);
        container.addNew(new IdentifiableMessageHandler<>("exception") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                throw new HandleException("Throw HandleException", new Exception());
            }
        });
        HandleException he = assertThrows(HandleException.class, () -> container.invokeAll(message));
        assertEquals("Throw HandleException", he.getMessage());
    }
}
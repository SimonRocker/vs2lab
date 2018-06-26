package de.hska.lkit.demo.redis.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import static java.net.InetAddress.getLocalHost;

@Controller
public class GreetingMessagingController {

    @Autowired
    private SimpMessagingTemplate msgTemplate;
    @Autowired
    private RedisMessageListenerContainer container;
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private MessageListenerAdapter listenerAdapter;

    @MessageMapping("/app/hello")
    public void greeting(HelloMessage message) throws Exception {
        System.out.println("###____________________________________________________________> hab was empfangen");
        Thread.sleep(3000); // simulated delay
        msgTemplate.convertAndSend("/topic/greetings", new Greeting("Another Hello, " + message.getName() + "!"));
    }

    @MessageMapping("/app/newPost")
    public void newPostIsComing(String message) throws Exception {


        /*
        global und personal timeline soll nun benachrichtig werden dass es neuen post gibt
         */
        // Post an stomp in helloCli.js
        template.convertAndSend("post", message);
    }

}

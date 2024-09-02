package com.anish.apiGateway.service.UsersControllers;

import com.anish.apiGateway.service.KafkaServices.RegisterUserKafkaService;
import com.anish.apiGateway.service.RequestModels.RegisterUserModel;
import com.anish.apiGateway.service.ResponseModels.UserRegistrationResponseModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/v1")
public class V1ApiController {

    private final Map<String, DeferredResult<ResponseEntity<UserRegistrationResponseModel>>> pendingRequests = new ConcurrentHashMap<>();

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RegisterUserKafkaService registerUserKafkaService;


    @KafkaListener(topics = {"registration-response","registration-error-response"}, groupId = "my-group-id")
    public void consume(Map<String, Object> message) {
        System.out.println("Received message: " + message);
        String correlationId = (String) message.get("correlationId");
        message.remove("correlationId");
        // Handle the response in the controller
        handleResponse(correlationId, message);

    }


    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(){
        return "Hello World";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public DeferredResult<ResponseEntity<UserRegistrationResponseModel>> registerUser(@RequestBody RegisterUserModel registerUserModel){


        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        Map<String,Object> requestMap = objectMapper.convertValue(registerUserModel, Map.class);


        String correlationId = UUID.randomUUID().toString();


        DeferredResult<ResponseEntity<UserRegistrationResponseModel>> deferredResult = new DeferredResult<>(50000L); // 5 seconds timeout


        pendingRequests.put(correlationId, deferredResult);
        requestMap.put("correlationId", correlationId);
        registerUserKafkaService.sendMessage("messageKey",requestMap);
        deferredResult.onTimeout(() -> {
            pendingRequests.remove(correlationId);
            deferredResult.setResult(ResponseEntity.status(504).body((UserRegistrationResponseModel) Map.of("error", "Request timed out")));
        });



        return deferredResult;
    }

    public void handleResponse(String correlationId, Map<String, Object> response) {
        DeferredResult<ResponseEntity<UserRegistrationResponseModel>> deferredResult = pendingRequests.remove(correlationId);
        if (deferredResult != null) {
            deferredResult.setResult(ResponseEntity.ok(objectMapper.convertValue(response,UserRegistrationResponseModel.class))); // Complete the request with the response
        }
    }


}

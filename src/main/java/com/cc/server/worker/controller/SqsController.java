package com.cc.server.worker.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageVisibility;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.json.Jackson;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;

import com.cc.server.worker.model.LogginModel;
import com.cc.server.worker.model.NumberListRequest;
import com.cc.server.worker.services.LogginService;
import com.cc.server.worker.services.SqsService;
import com.cc.server.worker.websocket.config.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Equivalence.Wrapper;
@SpringBootApplication(exclude = {ContextStackAutoConfiguration.class})

@RestController
@RequestMapping(value = "/api/sqs")

@CrossOrigin
public class SqsController {
	@Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private SqsService sqsService;
    @Autowired
    private LogginService logginService;
    private Message_Handler_Singleton messagePip;
    public static final Logger LOGGER = LoggerFactory.getLogger(SqsController.class);
    @Value("${aws.endpoint.number_list_sender}")
    private String endpoint; 
    private static final String reciever_queue_num_list="sqs_number_list_reciever_poll";
    private final String sender_queue_num_list="sqs_number_list_sender_poll";	
    
    @SqsListener(value = sender_queue_num_list, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void getMessageFromSqs( String message, 
			  @Header("MessageId") String messageId,
			  @Header("LogicalResourceId") String logicalResourceId,
			  @Header("ApproximateReceiveCount") String approximateReceiveCount,
			  @Header("ApproximateFirstReceiveTimestamp") String approximateFirstReceiveTimestamp,
			  @Header("SentTimestamp") String sentTimestamp,
			  @Header("ReceiptHandle") String receiptHandle,
			  @Header("Visibility") QueueMessageVisibility visibility,
			  @Header("SenderId") String senderId,
			  @Header("contentType") String contentType,
			  @Header("lookupDestination") String lookupDestination
	  ) {
			LOGGER.info("Received message= {}", message);
			LogginModel logModel=new LogginModel();
   			logModel.messageId=messageId;
   			logModel.message=message;
   			logModel.logicalResourceId=logicalResourceId;
   			logModel.approximateReceiveCount=approximateReceiveCount;
   			logModel.approximateFirstReceiveTimestamp=approximateFirstReceiveTimestamp;
   			logModel.sentTimestamp=sentTimestamp;
   			logModel.receiptHandle=receiptHandle;
   			logModel.senderId=senderId;
   			logModel.contentType=contentType;
   			logModel.lookupDestination=lookupDestination;
   			try {
				logginService.addLoggin(logModel);
				String logText=messageId+"\n"+message+"\n"+logicalResourceId+
						"\n"+approximateReceiveCount+"\n"+approximateFirstReceiveTimestamp
						+"\n"+sentTimestamp+"\n"+receiptHandle
						+"\n"+senderId+"\n"+contentType
						+"\n"+lookupDestination;
				logginService.addLogginString(logText);
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ObjectMapper mapper = new ObjectMapper();
			NumberListRequest request=new NumberListRequest();
			try {
				request = mapper.readValue(message, NumberListRequest.class);
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			final String average=sqsService.getResults(request);
			request.output=average;
			String messageBody=Jackson.toJsonString(request);
			queueMessagingTemplate.convertAndSend(reciever_queue_num_list, messageBody);
			LOGGER.info("Successfully Dispatched to queue");
    	}
}

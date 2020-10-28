package com.cc.server.worker.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cc.server.worker.model.NumberListRequest;


@Service 
public class SqsService {
	@Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    
	public String getResults(NumberListRequest request) {
		double mean=calculateMean(request);
		double median=calculateMedian(request.input);
		double min=calculateMin(request.input);
		double max=calculateMax(request.input);
		String result="Min {"+min+"}, max{"+max+"}, median{"+median+"}, mean{"+mean+"}";
		return result;
	}
	
	public double calculateMean(NumberListRequest request) {
		double total=0;
		double res=0;
		for(int num:request.input) {
			total+=num;
		}
		res=total/request.input.size();
		return res;
	}
	public double calculateMin(List<Integer> list) {
		double min=0;
		min=Collections.min(list);
		return min;
	}
	public double calculateMax(List<Integer> list) {
		double max=0;
		max=Collections.max(list);
		return max;
	}
	
	public double calculateMedian(List<Integer> list) {
		int[] numArray = list.stream().mapToInt(i->i).toArray();
		Arrays.sort(numArray);
		double median;
		if (numArray.length % 2 == 0)
		    median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
		else
		    median = (double) numArray[numArray.length/2];
		return median;
	}
	
	public void sendMessages(String queue, String message) {
		queueMessagingTemplate.convertAndSend(queue, message);
	}
	
}

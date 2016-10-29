package com.egima.ussdmenuserver;

import java.util.ArrayDeque;
import java.util.Deque;

public class IStack {
	  private Deque<String> data = new ArrayDeque<String>();

	  public void push(String element) {
	    data.addFirst(element);
	  }

	  public String pop() {
	    return data.removeFirst();
	  }

	  public String peek() {
	    return data.peekFirst();
	  }

	  public String toString() {
	    return data.toString();
}
}
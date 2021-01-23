package com.rtaware.jigsaw.event;

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Category;

@Category("JigsawEvent")
@Label("JigsawEvent")
@Name("com.rtaware.JigsawEvent")
public class JigsawEvent extends Event {

	public String getJigsawOperation() {
		return jigsawOperation;
	}

	public void setJigsawOperation(String jigsawOperation) {
		this.jigsawOperation = jigsawOperation;
	}

	public String getJigsawStatus() {
		return jigsawStatus;
	}

	public void setJigsawStatus(String jigsawStatus) {
		this.jigsawStatus = jigsawStatus;
	}

	public int getJigsawAttempt() {
		return jigsawAttempt;
	}

	public void setJigsawAttempt(int jigsawAttempt) {
		this.jigsawAttempt = jigsawAttempt;
	}

	public int getLhOperand() {
		return lhOperand;
	}

	public void setLhOperand(int lhOperand) {
		this.lhOperand = lhOperand;
	}

	public int getRhOperand() {
		return rhOperand;
	}

	public void setRhOperand(int rhOperand) {
		this.rhOperand = rhOperand;
	}

	public int getEnteredValue() {
		return enteredValue;
	}

	public void setEnteredValue(int enteredValue) {
		this.enteredValue = enteredValue;
	}

	public int getActualValue() {
		return actualValue;
	}

	public void setActualValue(int actualValue) {
		this.actualValue = actualValue;
	}

	@Label("Jigsaw Attempt")
	private int jigsawAttempt;

	@Label("LH Operand")
	private int lhOperand;

	@Label("RH Operand")
	private int rhOperand;

	@Label("Jigsaw Operation")
	private String jigsawOperation;

	@Label("Entered Value")
	private int enteredValue;

	@Label("Actual Value")
	private int actualValue;

	@Label("Jigsaw Status")
	private String jigsawStatus;

}
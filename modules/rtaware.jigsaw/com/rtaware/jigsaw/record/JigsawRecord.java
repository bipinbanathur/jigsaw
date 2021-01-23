package com.rtaware.jigsaw.record;

public record JigsawRecord(int jigsawAttempt, int lhOperand, int rhOperand, String jigsawOperation, int enteredValue,
		int actualValue, String jigsawStatus) {
}
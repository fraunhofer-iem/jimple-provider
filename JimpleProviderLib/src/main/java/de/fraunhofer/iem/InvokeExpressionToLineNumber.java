package de.fraunhofer.iem;

import lombok.Data;

@Data
public class InvokeExpressionToLineNumber {
    final String invokedMethodSignature;
    final int lineNumber;
}

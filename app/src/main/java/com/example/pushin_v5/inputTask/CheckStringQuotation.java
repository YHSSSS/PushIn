package com.example.pushin_v5.inputTask;

/***********************************************************************
 This file is used to replace some of the marks in the string so that they
 can be detected and replaced back on the server.
 ***********************************************************************/
public class CheckStringQuotation {
    public static String quotation(String input) {
        if (input.contains("'")) input = input.replaceAll("'", "\\\\\\'");
        if (input.contains("\"")) input = input.replaceAll("\"", "\\\\\"");
        if (input.contains(" ")) input = input.replaceAll(" ", "\\\\s");
        if (input.contains("&")) input = input.replaceAll("&", "\\\\aps");
        if (input.contains(":")) input = input.replaceAll(":", "\\\\:");
        if (input.contains("\n")) input = input.replaceAll("\n", "\\\\n");
        return input;
    }
}

package ru.edu.spbstu.client.utils;

import ru.edu.spbstu.client.exception.InvalidDataException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Verifiers {
    public static void checkEmail(String email)
    {
        if(email.length()<4||email.length()>128)
        {
            throw new InvalidDataException("InvalidEmailSizeError");
        }
        final String EMAIL_PATTERN =
                "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        +"([^-][A-Za-z0-9-]*)?(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{1,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher m=pattern.matcher(email);
        if(!m.matches())
        {
            throw new InvalidDataException("BadFormatEmailErrorText");
        }
    }


    public static void checkLogin(String login)
    {
        if(login.length()<1||login.length()>50)
        {
            throw new InvalidDataException("InvalidLoginSizeError");
        }

        final String LOGIN_PATTERN =
                "(([A-Za-z0-9А-Яа-яёЁ&&[^\\\\\\_]])|[.\\-])+";
        Pattern pattern = Pattern.compile(LOGIN_PATTERN);
        Matcher m=pattern.matcher(login);
        if(!m.matches())
        {
            throw new InvalidDataException("BadFormatLoginErrorText");
        }

    }
    public static void checkChatName(String chatName)
    {
        if(chatName.length()<1||chatName.length()>50)
        {
            throw new InvalidDataException("InvalidChatSizeError");
        }
        final String CHAT_NAME_PATTERN =
                "^([A-Za-z0-9А-Яа-яёЁ&&[^\\\\\\_]]|[.\\-,!?()])" +
                        "(([A-Za-z0-9А-Яа-яёЁ&&[^\\\\\\_]]|[.\\-,!?()])|[ ])*" +
                        "([A-Za-z0-9А-Яа-яёЁ&&[^\\\\\\_]]|[.\\-,!?()])$";
        Pattern pattern;
        if(chatName.length()!=1) {
            pattern = Pattern.compile(CHAT_NAME_PATTERN);
        }
        else
        {
            pattern = Pattern.compile("^([A-Za-z0-9А-Яа-яёЁ&&[^\\\\\\_]]|[.\\-,!?()])");
        }
        Matcher m=pattern.matcher(chatName);
        if(!m.matches())
        {
            throw new InvalidDataException("InvalidChatFormatError");
        }

    }
}

package io.minecast.exceptions;

public class MinecastException extends Exception {

    private int code;

    /**
     * Contains the Minecast error code and the response string
     *
     * @param code Error Code
     * @param e    Error Description
     */
    public MinecastException(int code, String e) {
        super(e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}

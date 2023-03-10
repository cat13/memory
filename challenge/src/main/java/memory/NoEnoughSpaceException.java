package memory;

public final class NoEnoughSpaceException extends RuntimeException{
    public NoEnoughSpaceException(String message){
        super(message);
    }
}

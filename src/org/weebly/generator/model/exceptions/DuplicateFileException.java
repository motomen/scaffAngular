package org.weebly.generator.model.exceptions;

/**
 * Exception thrown when a duplicate file is being created.
 * Created by IronMan on 7/8/14.
 */
public class DuplicateFileException  extends BaseException {

    /**
     * Creates an instance of DuplicateFileException
     */
    public DuplicateFileException(){
        super("DuplicateFileException", "The file you are trying to create already exists", ExceptionCode.DUPLICATE_FILE_EXISTS);
    }
}

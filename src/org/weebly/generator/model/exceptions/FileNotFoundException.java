package org.weebly.generator.model.exceptions;

/**
 * Exception used when the file is not found during a RUD operations
 * Created by IronMan on 7/8/14.
 */
public class FileNotFoundException extends BaseException {

    /**
     * Creates an instance of FileNotFoundException
     */
    public FileNotFoundException(){
        super("FileNotFoundException", "File could not be located on disk", ExceptionCode.FILE_NOT_FOUND);
    }
}

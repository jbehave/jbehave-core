package org.jbehave.core.io.rest;

/**
 * Uploads text resource to REST path.
 * 
 * @author Mauro Talevi
 */
public interface ResourceUploader {

    void uploadResourceAsText(String resourcePath, String text);
    
}

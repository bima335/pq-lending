package com.pq;

import java.util.logging.Logger;

public class App 
{
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main( String[] args )
    {
       logger.info( "Hello World!" );
import com.pq.presentation.CLIApplication;

public class App {
    public static void main(String[] args) {
        new CLIApplication().run();
    }
}

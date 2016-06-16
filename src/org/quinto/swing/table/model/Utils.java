package org.quinto.swing.table.model;

import java.awt.FontMetrics;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Utils {
  private static final Logger LOGGER = Logger.getLogger( Utils.class );
  
  public static boolean equals( Object o1, Object o2 ) {
    return o1 == null ? o2 == null : o2 != null && o1.equals( o2 );
  }
  
  public static String rpad( String s, int n ) {
    if ( null == s )
      return null;
    if ( n <= 0 )
      return "";
    if ( n < s.length() )
      return s.substring( 0, n );
    return String.format( "%1$-" + n + "s", s );
  }
  
  public static String rpad( String text, char padChar, int length ) {
    if ( text == null )
      return null;
    if ( length <= 0 || length < text.length() )
      return text;
    StringBuilder result = new StringBuilder( text );
    while ( result.length() < length )
      result.append( padChar );
    return result.toString();
  }
  
  public static void initSimpleConsoleLogger() {
    String logProps = "log4j.rootLogger=DEBUG, Console\n" +
                      "log4j.appender.Console=org.apache.log4j.ConsoleAppender\n" +
                      "log4j.appender.Console.layout=org.apache.log4j.PatternLayout\n" +
                      "log4j.appender.Console.layout.ConversionPattern=%d{dd.MM.yy HH:mm:ss,SSS} [%p] - %m%n\n";
    Properties props = new Properties();
    try {
      props.load( new ByteArrayInputStream( logProps.getBytes() ) );
    } catch ( IOException e ) {
    }
    PropertyConfigurator.configure( props );
  }
  
  public static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    } catch ( InstantiationException e ) {
      LOGGER.error( null, e );
    } catch ( ClassNotFoundException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( UnsupportedLookAndFeelException e ) {
      LOGGER.error( null, e );
    }
  }

  public static void updateComponentTreeUI() {
    for ( Window window : Window.getWindows() )
      updateComponentTreeUI( window );
  }

  private static void updateComponentTreeUI( Window window ) {
    SwingUtilities.updateComponentTreeUI( window );
    for ( Window w : window.getOwnedWindows() )
      updateComponentTreeUI( w );
  }
}
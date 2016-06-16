package org.quinto.swing.table.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;
import org.quinto.swing.table.model.Utils;

public class TutorialCustomRenderer {
  public static void main( String args[] ) throws Exception {
    Utils.initSimpleConsoleLogger();
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelField( "USER_ID", "User identifier" ),
      new ModelFieldGroup( "NAME", "Person name" )
        .withChild( new ModelField( "FIRST_NAME", "First name" ) )
        .withChild( new ModelField( "LAST_NAME", "Last name" ) ),
      new ModelField( "PHONE", "Phone number" )
    };
    
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ )
      rows[ i ] = new ModelRow( fields.length );
    
    ModelData data = new ModelData( groups );
    data.setRows( rows );
    
    data.setValue( 0, "FIRST_NAME", "John" );
    data.setValue( 0, "LAST_NAME", "Doe" );
    data.setValue( 1, "FIRST_NAME", "Jane" );
    data.setValue( 1, "LAST_NAME", "Doe" );
    
    JBroTable table = new JBroTable( data );
    
    final JPanel p = new JPanel( new GridLayout( 1, 1 ) ) {
      @Override
      public void paint( Graphics g ) {
        super.paint( g );
        g.setColor( new Color( 220, 50, 50, 50 ) );
        g.fillRect( 0, 0, getWidth(), getHeight() );
      }
    };
    
    table.getTableHeader().getUI().setCustomRenderer( new CustomTableHeaderRenderer() {
      @Override
      public Component getTableCellRendererComponent( Component originalComponent, JBroTable table, Object value, boolean isSelected, boolean hasFocus, boolean isDragged, int row, int viewColumn, int modelColumn, IModelFieldGroup dataField ) {
        if ( dataField == null || !( originalComponent instanceof JLabel ) )
          return originalComponent;
        JLabel ret = ( JLabel )originalComponent;
        String fieldName = dataField.getIdentifier();
        ret.setHorizontalAlignment( "LAST_NAME".equals( fieldName ) ? SwingConstants.RIGHT : SwingConstants.LEFT );
        ret.setFont( ret.getFont().deriveFont( "NAME".equals( fieldName ) ? Font.BOLD : Font.PLAIN ) );
        ret.setOpaque( !"USER_ID".equals( fieldName ) );
        if ( "USER_ID".equals( fieldName ) ) {
          if ( ret.getParent() != p )
            p.add( ret );
          return p;
        }
        return ret;
      }
    } );
    
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( table.getScrollPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}
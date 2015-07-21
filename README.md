# JBroTable
JTable with a groupable header.

The implementation is based on [this](http://stackoverflow.com/a/20208995/4540645) StackOverflow answer by [MadProgrammer](http://stackoverflow.com/users/992484/madprogrammer).

Supported Look & Feels:
- Nimbus
- Windows
- Windows Classic
- Motif
- Metal (high contrast, steel, ocean)
- GTK

Tested on Windows 2003, 7 and 8 with standard (Luna, Aero, Metro) and with classic themes. Tested on OpenSUSE 11 with GNOME (GTK+ 2). Not tested on Mac OS. _May_ work on Aqua L&F properly too.

Supported features (original StackOverflow answer):
- Stops dragging a column at table end
- Stops dragging a column at columns group end

New features:
- Arbitrary number of rows in a header
- Stops dragging at freezed column (such columns may be set in a table model via API)
- Generic support for L&Fs (it means they're not reimplemented, they're used as delegates for rendering)
- Easy to use table model API
- Table header cells highlighting on mouse over
- Table cells spans API to merge cells inside a table itself (not in a header)
- Drawing of dragged columns group in a table itself (not only in a header)
- Partial test coverage
- Setting custom rowspan. Rowspan is calculated automatically in a greedy way, but sometimes such a picture is needed (and it can't be calculated automatically):

![Rowspan](/github/rowspan.png)

ASCII (if a picture is not shown):

```
+---------------+---------------+
| A             |               |
+-------+-------+ D             |
|       |       |               |
| B     | C     +-------+-------+
|       |       | E     | F     |
+-------+-------+-------+-------+
```

Dependencies:
- Java 6 and higher
- Apache Log4J
- JUnit (for tests only)

[__Animated demo__](/github/demo.gif) (2.5M)

Sample usage:

```java
package org.quinto.swing.table.view;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.quinto.swing.table.model.IModelFieldGroup;
import org.quinto.swing.table.model.ModelData;
import org.quinto.swing.table.model.ModelField;
import org.quinto.swing.table.model.ModelFieldGroup;
import org.quinto.swing.table.model.ModelRow;

public class Sample {
  public static void main( String args[] ) throws Exception {
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

    // Hierarchically create columns and column groups.
    // Each node of columns tree is an instance of IModelFieldGroup.
    // Leafs are always ModelFields.
    // Roots can be either ModelFields or ModelFieldGroups.
    IModelFieldGroup groups[] = new IModelFieldGroup[] {
      new ModelFieldGroup( "A", "A" )
        .withChild( new ModelField( "B", "B" ) )
        .withChild( new ModelField( "C", "C" ).withRowspan( 2 ) ), // Custom rowspan set.
      new ModelFieldGroup( "D", "D" )
        .withChild( new ModelField( "E", "E" ) )
        .withChild( new ModelField( "F", "F" ) ),
      new ModelField( "G", "G" ),
      new ModelFieldGroup( "H", "H" )
        .withChild( new ModelFieldGroup( "I", "I" )
                      .withChild( new ModelField( "J", "J" ) ) )
        .withChild( new ModelField( "K", "K" ) )
        .withChild( new ModelFieldGroup( "L", "L" )
                      .withChild( new ModelField( "M", "M" ) )
                      .withChild( new ModelField( "N", "N" ) ) )
    };

    // Get leafs of columns tree.
    ModelField fields[] = ModelFieldGroup.getBottomFields( groups );
    
    // Sample data.
    ModelRow rows[] = new ModelRow[ 10 ];
    for ( int i = 0; i < rows.length; i++ ) {
      rows[ i ] = new ModelRow( fields.length );
      for ( int j = 0; j < fields.length; j++ )
        rows[ i ].setValue( j, fields[ j ].getCaption() + i );
    }
    
    // Table.
    ModelData data = new ModelData( groups );
    data.setRows( rows );
    JBroTable table = new JBroTable( data );

    // Window.
    JFrame frame = new JFrame( "Test" );
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setLayout( new FlowLayout() );
    frame.add( new JScrollPane( table ) );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( true );
  }
}
```

Result (Windows 8 theme):

![Result](/github/windows8.png)

Released into public domain.
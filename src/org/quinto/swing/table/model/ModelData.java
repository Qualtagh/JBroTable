package org.quinto.swing.table.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Array of data.
 */
public class ModelData implements Serializable {
  private static final long serialVersionUID = 1L;
  /** This constant is returned if the requested field and its coordinates are not found. */
  public static final int NO_FIELD_COORDINATES[] = { -1, -1 };

  private ModelRow rows[];
  private ModelField fields[];
  private transient Map< String, Integer > fieldsIndex = new HashMap< String, Integer >();
  private transient Map< String, int[] > fieldGroupsIndex = new HashMap< String, int[] >();
  private transient List< IModelFieldGroup[] > fieldGroups = new ArrayList< IModelFieldGroup[] >();

  public ModelData() {
    this( null );
  }

  public ModelData( ModelField fields[] ) {
    setFields( fields );
  }

  public ModelData( IModelFieldGroup fieldGroups[] ) {
    this( ModelFieldGroup.getBottomFields( fieldGroups ) );
  }
  
  private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    indexateFields();
  }

  public void setFields( ModelField fields[] ) {
    if ( ModelField.equals( this.fields, fields ) ) {
      // Fields are equal but object references may differ, so this set of a new reference is required.
      this.fields = fields;
    } else {
      if ( this.fields != null )
        rows = null;
      this.fields = fields;
      indexateFields();
    }
  }

  public ModelField[] getFields() {
    return fields;
  }

  public int getFieldsCount() {
    return getFieldsCount( false );
  }
  
  public int getFieldsCount( boolean visibleOnly ) {
    if ( fields == null ) {
      return 0;
    }
    return visibleOnly ? ModelField.getVisibleFieldsCount( fields ) : fields.length;
  }

  public List< IModelFieldGroup[] > getFieldGroups() {
    return fieldGroups;
  }
  
  public void setRows( ModelRow rows[] ) {
    this.rows = rows;
  }

  public ModelRow[] getRows() {
    return rows;
  }

  public int getRowsCount() {
    return rows == null ? 0 : rows.length;
  }
  
  /**
   * Get index of a field in a table data model by a given field identifier.
   * @param identifier field identifier
   * @return index of a field, or -1 if such field doesn't exist
   */
  public int getIndexOfModelField( String identifier ) {
    Integer ret = fieldsIndex.get( identifier );
    return ret == null ? -1 : ret;
  }
  
  /**
   * Get coordinates of a field (or a field group) by a given field identifier.
   * A pair ( column, row ) is returned.
   * @param identifier field group identifier
   * @return a pair ( x, y ), or ( -1, -1 ) if such field does not exist. Non null.
   */
  public int[] getIndexOfModelFieldGroup( String identifier ) {
    int ret[] = fieldGroupsIndex.get( identifier );
    return ret == null ? NO_FIELD_COORDINATES : ret;
  }
  
  public Object getValue( int row, String fieldName ) {
    if ( row < 0 || row >= getRowsCount() ) {
      return null;
    }
    int fIdx = getIndexOfModelField( fieldName );
    if ( fIdx == -1 ) {
      return null;
    }
    return rows[ row ] == null ? null : rows[ row ].getValue( fIdx );
  }

  public Object getValue( int row, int field ) {
    if ( row < 0 || row >= getRowsCount() ) {
      return null;
    }
    if ( field < 0 || field >= getFieldsCount() ) {
      return null;
    }
    return rows[ row ] == null ? null : rows[ row ].getValue( field );
  }
  
  public void setValue( int row, String fieldName, Object value ) {
    if ( row < 0 || row >= getRowsCount() ) {
      return;
    }
    int fIdx = getIndexOfModelField( fieldName );
    if ( fIdx == -1 ) {
      return;
    }
    rows[ row ].getValues()[ fIdx ] = value;
  }

  public void setValue( int row, int field, Object value ) {
    if ( row < 0 || row >= getRowsCount() ) {
      return;
    }
    if ( field < 0 || field >= getFieldsCount() ) {
      return;
    }
    rows[ row ].getValues()[ field ] = value;
  }

  private void indexateFields() {
    if ( fields == null )
      fields = new ModelField[ 0 ];
    Map<String, Integer> newFieldsIndex = new HashMap<String, Integer>( fields.length );
    for ( int i = 0; i < fields.length; i++ )
      if ( newFieldsIndex.put( fields[ i ].getIdentifier(), i ) != null )
        throw new IllegalArgumentException( "Duplicated identifier: " + fields[ i ].getIdentifier() );
    fieldsIndex = newFieldsIndex;
    fieldGroupsIndex = new HashMap<String, int[]>();
    IModelFieldGroup upper[] = ModelFieldGroup.getUpperFieldGroups( fields );
    ModelField bottom[] = ModelFieldGroup.getBottomFields( upper );
    if ( !Arrays.equals( bottom, fields ) )
      throw new IllegalArgumentException( "Field groups contain more children than specified in fields array.\nActual: " + Arrays.toString( fields ) + "\nExpected: " + Arrays.toString( bottom ) );
    calculateRowspan( fields );
    for ( IModelFieldGroup group : upper )
      group.setRowspan( 0 );
    calculateRowspanForUpper( upper );
    fieldGroups = new ArrayList< IModelFieldGroup[] >();
    addFieldGroupsRecursively( upper, 0, new HashMap< String, Integer >() );
  }

  private void calculateRowspan( IModelFieldGroup fieldGroups[] ) {
    List< IModelFieldGroup > parents = new ArrayList< IModelFieldGroup >();
    FieldGroupsLoop:
    for ( IModelFieldGroup fieldGroup : fieldGroups ) {
      ModelFieldGroup parent = fieldGroup.getParent();
      if ( parent != null && parent.getChildrenRowspan() == -1 ) {
        int childrenRowspan = 0;
        for ( IModelFieldGroup child : parent.getChildren() ) {
          int rowspan = child.getRowspan();
          if ( rowspan < 1 )
            rowspan = 1;
          if ( child instanceof ModelFieldGroup ) {
            int childChildrenRowspan = ( ( ModelFieldGroup )child ).getChildrenRowspan();
            if ( childChildrenRowspan >= 0 )
              rowspan += childChildrenRowspan;
            else
              continue FieldGroupsLoop;
          }
          if ( rowspan > childrenRowspan )
            childrenRowspan = rowspan;
        }
        parent.setChildrenRowspan( childrenRowspan );
        for ( IModelFieldGroup child : parent.getChildren() ) {
          int rowspan = childrenRowspan;
          if ( child instanceof ModelFieldGroup ) {
            rowspan -= ( ( ModelFieldGroup )child ).getChildrenRowspan();
          }
          child.setRowspan( rowspan );
        }
        parents.add( parent );
      }
    }
    if ( !parents.isEmpty() )
      calculateRowspan( parents.toArray( new IModelFieldGroup[ parents.size() ] ) );
  }
  
  private void calculateRowspanForUpper( IModelFieldGroup upper[] ) {
    int childrenRowspan = 0;
    for ( IModelFieldGroup fieldGroup : upper ) {
      int rowspan = fieldGroup.getRowspan();
      if ( rowspan < 1 )
        rowspan = 1;
      if ( fieldGroup instanceof ModelFieldGroup ) {
        rowspan += ( ( ModelFieldGroup )fieldGroup ).getChildrenRowspan();
      }
      if ( rowspan > childrenRowspan )
        childrenRowspan = rowspan;
    }
    for ( IModelFieldGroup fieldGroup : upper ) {
      int rowspan = childrenRowspan;
      if ( fieldGroup instanceof ModelFieldGroup ) {
        rowspan -= ( ( ModelFieldGroup )fieldGroup ).getChildrenRowspan();
      }
      fieldGroup.setRowspan( rowspan );
    }
  }

  private void addFieldGroupsRecursively( IModelFieldGroup upper[], int level, Map< String, Integer > fieldPosIndex ) {
    fieldGroups.add( upper );
    int x = 0;
    int posX = 0;
    List< IModelFieldGroup > current = new ArrayList< IModelFieldGroup >();
    for ( IModelFieldGroup fieldGroup : upper ) {
      int rowspan = fieldGroup.getRowspan();
      String identifier = fieldGroup.getIdentifier();
      int prev[] = fieldGroupsIndex.get( identifier );
      boolean groupFinished = rowspan == 1;
      if ( prev == null ) {
        fieldGroupsIndex.put( identifier, new int[]{ x, level } );
        fieldPosIndex.put( identifier, posX );
      } else {
        int prevX = fieldPosIndex.get( identifier );
        if ( prevX != posX )
          throw new IllegalArgumentException( "Field group should be unbroken: " + fieldGroup );
        if ( !groupFinished )
          groupFinished = rowspan == 1 + level - prev[ 1 ];
      }
      if ( groupFinished ) {
        if ( fieldGroup instanceof ModelFieldGroup ) {
          ModelFieldGroup group = ( ModelFieldGroup )fieldGroup;
          current.addAll( group.getChildren() );
        }
      } else
        current.add( fieldGroup );
      x++;
      posX += fieldGroup.getColspan();
    }
    if ( !current.isEmpty() )
      addFieldGroupsRecursively( current.toArray( new IModelFieldGroup[ current.size() ] ), level + 1, fieldPosIndex );
  }

  /**
   * A method for testing purposes.
   * @return a string representation of this array's header (fields and their groups)
   * @deprecated should not be used in production code
   */
  public String getHeaderString() {
    int maxIdentifierLength = 0;
    for ( String identifier : fieldGroupsIndex.keySet() )
      if ( identifier.length() > maxIdentifierLength )
        maxIdentifierLength = identifier.length();
    if ( maxIdentifierLength == 0 )
      maxIdentifierLength = 1;
    maxIdentifierLength++;
    int maxX = fields.length;
    StringBuilder sb = new StringBuilder();
    for ( IModelFieldGroup groups[] : fieldGroups ) {
      if ( sb.length() > 0 )
        sb.append( '\n' );
      else
        sb.append( Utils.rpad( "", '+', maxX * ( maxIdentifierLength + 3 ) ) ).append( '\n' );
      for ( IModelFieldGroup group : groups ) {
        sb.append( "+ " ).append( Utils.rpad( group.getIdentifier(), ( maxIdentifierLength + 3 ) * group.getColspan() - 3 ) ).append( '+' );
      }
      sb.append( '\n' ).append( Utils.rpad( "", '+', maxX * ( maxIdentifierLength + 3 ) ) );
    }
    return sb.toString();
  }

  /**
   * Scan field groups starting from bottom.
   * <b>For testing purposes only.</b>
   * @param depthFirst scan field groups in depth-first manner if true, otherwise scan in breadth-first manner
   * @return field groups sequence
   * @deprecated use {@link #getFieldGroups} instead
   */
  public Iterable< IModelFieldGroup > getAllFieldGroupsFromBottom( final boolean depthFirst ) {
    return new Iterable< IModelFieldGroup >() {
      private final LinkedList< IModelFieldGroup > list = new LinkedList< IModelFieldGroup >( Arrays.asList( fields ) );
      private final Set< String > visited = new HashSet< String >();
      
      @Override
      public Iterator< IModelFieldGroup > iterator() {
        return new Iterator< IModelFieldGroup >() {
          @Override
          public boolean hasNext() {
            return !list.isEmpty();
          }

          @Override
          public IModelFieldGroup next() {
            IModelFieldGroup ret = list.pollFirst();
            IModelFieldGroup parent = ret.getParent();
            if ( parent != null && visited.add( parent.getIdentifier() ) ) {
              if ( depthFirst )
                list.addFirst( parent );
              else
                list.addLast( parent );
            }
            return ret;
          }
        };
      }
    };
  }
  
  /**
   * Scan field groups starting from top.
   * @param depthFirst scan field groups in depth-first manner if true, otherwise scan in breadth-first manner
   * @return field groups sequence
   */
  public Iterable< IModelFieldGroup > getAllFieldGroupsFromTop( final boolean depthFirst ) {
    return new Iterable< IModelFieldGroup >() {
      private final LinkedList< IModelFieldGroup > list = new LinkedList< IModelFieldGroup >( fieldGroups.isEmpty() ? Collections.EMPTY_LIST : Arrays.asList( fieldGroups.get( 0 ) ) );
      
      @Override
      public Iterator< IModelFieldGroup > iterator() {
        return new Iterator< IModelFieldGroup >() {
          @Override
          public boolean hasNext() {
            return !list.isEmpty();
          }

          @Override
          public IModelFieldGroup next() {
            IModelFieldGroup ret = list.pollFirst();
            if ( ret instanceof ModelFieldGroup )
              list.addAll( depthFirst ? 0 : list.size(), ( ( ModelFieldGroup )ret ).getChildren() );
            return ret;
          }
        };
      }
    };
  }
}
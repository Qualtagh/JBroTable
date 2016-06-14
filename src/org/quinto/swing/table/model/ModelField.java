package org.quinto.swing.table.model;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Data field attributes.
 */
public class ModelField implements Serializable, Comparable< ModelField >, IModelFieldGroup {
  private static final long serialVersionUID = 3L;
  
  private String identifier;
  private String caption;
  private ModelFieldGroup parent;
  private int rowspan;
  private boolean fixed;
  private boolean manageable;
  private boolean visible;
  private Integer defaultWidth;

  public ModelField() {
    this( null, null );
  }

  public ModelField( String identifier, String caption ) {
    this.caption = caption;
    this.identifier = identifier;
    visible = true;
    manageable = true;
  }

  @Override
  public void setIdentifier( String identifier ) {
    if ( identifier == null )
      throw new IllegalArgumentException( "Trying to set a null identifier for the field " + this.identifier + " (" + caption + ")" );
    this.identifier = identifier;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Field ID. Should be unique in the whole array.
   * @param identifier field ID, usually a database field name
   * @return this
   */
  public ModelField withIdentifier( String identifier ) {
    setIdentifier( identifier );
    return this;
  }
  
  public void setVisible( boolean visible ) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }
  
  /**
   * Field visibility property.
   * @param visible true - this field should be visible in table, false - this column should be hidden
   * @return this
   */
  public ModelField withVisible( boolean visible ) {
    setVisible( visible );
    return this;
  }

  public Integer getDefaultWidth() {
    return defaultWidth;
  }

  public void setDefaultWidth( Integer defaultWidth ) {
    this.defaultWidth = defaultWidth;
  }
  
  /**
   * Initial column width.
   * <p>{@code null} value means that initial width is not set. A default one would be used (determined by Swing).</p>
   * @param defaultWidth initial column width
   * @return this
   */
  public ModelField withDefaultWidth( Integer defaultWidth ) {
    setDefaultWidth( defaultWidth );
    return this;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public void setCaption( String caption ) {
    this.caption = caption;
  }

  /**
   * Visible column title. Can start with &lt;html&gt; and contain hypertext markup.
   * @param caption column title
   * @return this
   */
  public ModelField withCaption( String caption ) {
    setCaption( caption );
    return this;
  }
  
  @Override
  public void setFixed( boolean fixed ) {
    this.fixed = fixed;
    if ( fixed && parent != null )
      parent.setFixed( fixed );
  }

  @Override
  public boolean isFixed() {
    return fixed;
  }

  /**
   * Fix (dock, freeze) this column at the left side of the table. Scrolling won't move it.
   * @param fixed {@code false} - this column can be scrolled, {@code true} - this column is fixed at the left side of the table<br>
   * {@code true} value would also fix parent columns group at the left side
   * @return this
   */
  public ModelField withFixed( boolean fixed ) {
    setFixed( fixed );
    return this;
  }
  
  @Override
  public void setManageable( boolean manageable ) {
    this.manageable = manageable;
    if ( !manageable && parent != null )
      parent.setManageable( manageable );
  }

  @Override
  public boolean isManageable() {
    return manageable;
  }

  /**
   * Determines user ability to move or hide this column.
   * @param manageable {@code true} - this column can be hidden, shown and moved, {@code false} - this column is fixed<br>
   * {@code false} value would also make parent unmanageable
   * @return this
   */
  public ModelField withManageable( boolean manageable ) {
    setManageable( manageable );
    return this;
  }

  @Override
  public ModelFieldGroup getParent() {
    return parent;
  }

  void setParent( ModelFieldGroup parent ) {
    this.parent = parent;
  }
  
  private ModelField withParent( ModelFieldGroup parent ) {
    setParent( parent );
    return this;
  }
  
  @Override
  public ModelField clone() {
    ModelField ret;
    try {
      ret = ( ModelField )super.clone();
    } catch ( CloneNotSupportedException e ) {
      ret = new ModelField();
    }
    return ret.withIdentifier( identifier )
              .withCaption( caption )
              .withVisible( visible )
              .withFixed( fixed )
              .withManageable( manageable )
              .withParent( parent )
              .withRowspan( rowspan )
              .withDefaultWidth( defaultWidth );
  }

  @Override
  public boolean equals( Object obj ) {
    return equals( obj, true );
  }
  
  @Override
  public boolean equals( Object obj, boolean withParent ) {
    if ( !( obj instanceof ModelField ) ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    ModelField field = ( ModelField )obj;
    return Utils.equals( identifier, field.identifier ) &&
           Utils.equals( caption, field.caption ) &&
           ( !withParent || Utils.equals( parent, field.parent ) );
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 19 * hash + ( this.identifier == null ? 0 : this.identifier.hashCode() );
    hash = 19 * hash + ( this.caption == null ? 0 : this.caption.hashCode() );
    return hash;
  }
  
  @Override
  public int getColspan() {
    return 1;
  }
  
  @Override
  public int getRowspan() {
    return rowspan;
  }
  
  @Override
  public void setRowspan( int rowspan ) {
    this.rowspan = rowspan;
  }
  
  /**
   * Explicitly sets rowspan for this column.
   * @param rowspan a quantity of header rows that should be combined together in one header cell
   * @return this
   */
  public ModelField withRowspan( int rowspan ) {
    setRowspan( rowspan );
    return this;
  }

  @Override
  public String toString() {
    return getIdentifier();
  }

  @Override
  public int compareTo( ModelField modelField ) {
    return modelField == null || modelField.identifier == null ? -1 : identifier == null ? 1 : identifier.compareTo( modelField.identifier );
  }

  public static boolean equals( ModelField fields1[], ModelField fields2[] ) {
    if ( fields1 == null && fields2 == null ) {
      return true;
    }
    if ( fields1 == null && fields2 != null ||
         fields1 != null && fields2 == null ||
         fields1.length != fields2.length ) {
      return false;
    }
    for ( int a = 0; a < fields1.length; a ++ ) {
      if ( !fields1[ a ].equals( fields2[ a ] ) ) {
        return false;
      }
    }
    return true;
  }
  
  public static int getVisibleFieldsCount( ModelField fields[] ) {
    if ( fields == null || fields.length == 0 ) {
      return 0;
    }
    int result = 0;
    for ( ModelField field : fields ) {
      if ( field.isVisible() ) {
        result ++;
      }
    }
    return result;
  }

  public static int getIndexOfModelField( ModelField fields[], String identifier ) {
    if ( fields == null || fields.length == 0 || identifier == null ) {
      return -1;
    }
    for ( int a = 0; a < fields.length; a ++ ) {
      if ( fields[ a ] != null && fields[ a ].identifier.equals( identifier ) ) {
        return a;
      }
    }
    return -1;
  }

  public static LinkedHashMap< String, Integer > getIndexes( ModelField fields[] ) {
    LinkedHashMap< String, Integer > ret = new LinkedHashMap< String, Integer >( fields.length );
    for ( int i = 0; i < fields.length; i++ )
      ret.put( fields[ i ].getIdentifier(), i );
    return ret;
  }
  
  public static ModelField[] copyOfModelFields( ModelField fields[] ) {
    if ( fields == null || fields.length == 0 )
      return fields;
    IModelFieldGroup upperFields[] = ModelFieldGroup.getUpperFieldGroups( fields );
    IModelFieldGroup ret[] = new IModelFieldGroup[ upperFields.length ];
    for ( int i = 0; i < upperFields.length; i++ )
      ret[ i ] = upperFields[ i ].clone();
    return ModelFieldGroup.getBottomFields( ret );
  }

  /**
   * Checks if {@code this} is a descendant of {@code ancestorCandidate}.
   * @param ancestorCandidate a probable ancestor of {@code this}
   * @param checkAncestorCandidateItself also return {@code true} if {@code this} == {@code ancestorCandidate}
   * @return true iff ancestorCandidate is an ancestor of {@code this}
   */
  public boolean isDescendantOf( IModelFieldGroup ancestorCandidate, boolean checkAncestorCandidateItself ) {
    if ( ancestorCandidate == null )
      return false;
    if ( !checkAncestorCandidateItself )
      ancestorCandidate = ancestorCandidate.getParent();
    while ( ancestorCandidate != null )
      if ( ancestorCandidate == this )
        return true;
      else
        ancestorCandidate = ancestorCandidate.getParent();
    return false;
  }
}
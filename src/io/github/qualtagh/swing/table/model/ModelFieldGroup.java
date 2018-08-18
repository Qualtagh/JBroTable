package io.github.qualtagh.swing.table.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A group of data fields.
 */
public class ModelFieldGroup implements IModelFieldGroup, Serializable {
  private static final long serialVersionUID = 4L;
  
  private String identifier;
  private String caption;
  private ModelFieldGroup parent;
  private int rowspan;
  private boolean fixed;
  private boolean manageable;
  private transient int colspan;
  private transient int childrenRowspan;
  private List< IModelFieldGroup > children = new ArrayList< IModelFieldGroup >();
  
  public ModelFieldGroup() {
    this( null, null );
  }
  
  public ModelFieldGroup( String identifier, String caption ) {
    this.identifier = identifier;
    this.caption = caption;
    manageable = true;
    childrenRowspan = -1;
  }
  
  private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    childrenRowspan = -1;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public void setCaption( String caption ) {
    this.caption = caption;
  }
  
  public ModelFieldGroup withCaption( String caption ) {
    setCaption( caption );
    return this;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public void setIdentifier( String identifier ) {
    this.identifier = identifier;
  }
  
  public ModelFieldGroup withIdentifier( String identifier ) {
    setIdentifier( identifier );
    return this;
  }

  @Override
  public ModelFieldGroup getParent() {
    return parent;
  }

  private void setParent( ModelFieldGroup parent ) {
    this.parent = parent;
  }
  
  private ModelFieldGroup withParent( ModelFieldGroup parent ) {
    setParent( parent );
    return this;
  }
  
  public IModelFieldGroup getChild( String identifier ) {
    if ( identifier == null ) {
      for ( IModelFieldGroup child : children )
        if ( child.getIdentifier() == null )
          return child;
    } else {
      for ( IModelFieldGroup child : children )
        if ( identifier.equals( child.getIdentifier() ) )
          return child;
    }
    return null;
  }

  public List< IModelFieldGroup > getChildren() {
    return Collections.unmodifiableList( children );
  }
  
  void removeChild( IModelFieldGroup child ) {
    children.remove( child );
    int childColspan = child.getColspan();
    ModelFieldGroup ancestor = this;
    while ( ancestor != null ) {
      ancestor.colspan -= childColspan;
      ancestor.childrenRowspan = -1;
      ancestor = ancestor.getParent();
    }
  }
  
  public ModelFieldGroup withChild( IModelFieldGroup child ) {
    if ( child != null ) {
      children.add( child );
      int childColspan = child.getColspan();
      ModelFieldGroup ancestor = this;
      while ( ancestor != null ) {
        ancestor.colspan += childColspan;
        ancestor.childrenRowspan = -1;
        ancestor = ancestor.getParent();
      }
      if ( child instanceof ModelField )
        ( ( ModelField )child ).setParent( this );
      else
        ( ( ModelFieldGroup )child ).setParent( this );
      if ( !child.isManageable() )
        setManageable( false );
      else if ( !isManageable() )
        child.setManageable( false );
      if ( child.isFixed() )
        setFixed( true );
      else if ( isFixed() )
        child.setFixed( true );
    }
    return this;
  }
  
  @Override
  public int getColspan() {
    if ( colspan == 0 )
      for ( IModelFieldGroup child : children )
        colspan += child.getColspan();
    return colspan;
  }
  
  @Override
  public int getRowspan() {
    return rowspan;
  }
  
  @Override
  public void setRowspan( int rowspan ) {
    this.rowspan = rowspan;
  }
  
  public ModelFieldGroup withRowspan( int rowspan ) {
    setRowspan( rowspan );
    return this;
  }

  public int getChildrenRowspan() {
    return childrenRowspan;
  }

  void setChildrenRowspan( int childrenRowspan ) {
    this.childrenRowspan = childrenRowspan;
  }

  @Override
  public ModelFieldGroup clone() {
    ModelFieldGroup ret;
    try {
      ret = ( ModelFieldGroup )super.clone();
      ret.childrenRowspan = -1;
      ret.colspan = 0;
      ret.children = new ArrayList< IModelFieldGroup >();
    } catch ( CloneNotSupportedException e ) {
      ret = new ModelFieldGroup();
    }
    ret.withIdentifier( identifier )
       .withCaption( caption )
       .withRowspan( rowspan )
       .withFixed( fixed )
       .withManageable( manageable )
       .withParent( parent );
    for ( IModelFieldGroup child : getChildren() )
      ret = ret.withChild( child.clone() );
    return ret;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + ( this.caption == null ? 0 : this.caption.hashCode() );
    hash = 59 * hash + ( this.identifier == null ? 0 : this.identifier.hashCode() );
    return hash;
  }

  @Override
  public boolean equals( Object obj ) {
    return equals( obj, true );
  }

  @Override
  public boolean equals( Object obj, boolean withParent ) {
    if ( !( obj instanceof ModelFieldGroup ) ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    final ModelFieldGroup other = ( ModelFieldGroup )obj;
    if ( !Utils.equals( caption, other.caption ) ||
         !Utils.equals( identifier, other.identifier ) ||
         withParent && !Utils.equals( parent, other.parent ) ||
         children.size() != other.children.size() )
      return false;
    for ( int i = 0; i < children.size(); i++ )
      if ( !children.get( i ).equals( other.children.get( i ), false ) )
        return false;
    return true;
  }

  @Override
  public String toString() {
    return getIdentifier();
  }
  
  public static ModelField[] getBottomFields( IModelFieldGroup fieldGroups[] ) {
    if ( fieldGroups == null )
      return null;
    Map< String, ModelField > ret = new LinkedHashMap< String, ModelField >();
    getBottomFields( Arrays.asList( fieldGroups ), ret );
    return ret.values().toArray( new ModelField[ ret.size() ] );
  }
  
  private static void getBottomFields( List< IModelFieldGroup > fieldGroups, Map< String, ModelField > ret ) {
    for ( IModelFieldGroup fieldGroup : fieldGroups ) {
      if ( fieldGroup instanceof ModelFieldGroup ) {
        ModelFieldGroup group = ( ModelFieldGroup )fieldGroup;
        getBottomFields( group.getChildren(), ret );
      } else if ( fieldGroup instanceof ModelField ) {
        ModelField field = ( ModelField )fieldGroup;
        ModelField prevField;
        if ( ( prevField = ret.put( field.getIdentifier(), field ) ) != null )
          throw new IllegalArgumentException( "Non-unique DB field name: " + field + " VS " + prevField );
      }
    }
  }
  
  public static IModelFieldGroup[] getUpperFieldGroups( IModelFieldGroup fields[] ) {
    if ( fields == null )
      return null;
    Map< String, Integer > uniqueGroups = new LinkedHashMap< String, Integer >();
    List< IModelFieldGroup > ret = new ArrayList< IModelFieldGroup >();
    for ( IModelFieldGroup field : fields ) {
      IModelFieldGroup parent = field.getParent();
      while ( parent != null ) {
        field = parent;
        parent = field.getParent();
      }
      Integer prev = uniqueGroups.put( field.getIdentifier(), uniqueGroups.size() );
      if ( prev == null ) {
        uniqueGroups.put( field.getIdentifier(), uniqueGroups.size() );
        ret.add( field );
      } else if ( !prev.equals( uniqueGroups.size() ) )
        throw new IllegalArgumentException( "Field groups should be unbroken and should have unique DB field names. Repeated: " + field.getIdentifier() );
    }
    return ret.toArray( new IModelFieldGroup[ ret.size() ] );
  }

  @Override
  public boolean isFixed() {
    return fixed;
  }

  @Override
  public void setFixed( boolean fixed ) {
    this.fixed = fixed;
    if ( !fixed ) {
      for ( IModelFieldGroup child : children )
        child.setFixed( fixed );
    } else if ( parent != null )
      parent.setFixed( fixed );
  }

  /**
   * Fix (dock, freeze) this column at the left side of the table. Scrolling won't move it.
   * @param fixed {@code false} - this column can be scrolled, {@code true} - this column is fixed at the left side of the table<br>
   * {@code true} would make all ancestor fields fixed at the left side, {@code false} would make all descendants scrollable (thus free from fixation)
   * @return this
   */
  public ModelFieldGroup withFixed( boolean fixed ) {
    setFixed( fixed );
    return this;
  }

  @Override
  public boolean isManageable() {
    return manageable;
  }

  @Override
  public void setManageable( boolean manageable ) {
    this.manageable = manageable;
    if ( manageable ) {
      for ( IModelFieldGroup child : children )
        child.setManageable( manageable );
    } else if ( parent != null )
      parent.setManageable( manageable );
  }

  /**
   * The ability of this field group to be moved by a user.
   * @param manageable {@code true} would make all descendant fields manageable, {@code false} would make all ancestors fixed
   * @return this
   */
  public ModelFieldGroup withManageable( boolean manageable ) {
    setManageable( manageable );
    return this;
  }
}
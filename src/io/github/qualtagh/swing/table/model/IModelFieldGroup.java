package io.github.qualtagh.swing.table.model;

public interface IModelFieldGroup extends Cloneable {
  String getCaption();
  void setCaption( String caption );
  
  String getIdentifier();
  void setIdentifier( String identifier );
  
  ModelFieldGroup getParent();
  
  int getColspan();
  
  int getRowspan();
  void setRowspan( int rowspan );
  
  boolean isManageable();
  void setManageable( boolean manageable );
  
  boolean isFixed();
  void setFixed( boolean fixed );
  
  IModelFieldGroup clone();
  boolean equals( Object obj, boolean withParent );
}
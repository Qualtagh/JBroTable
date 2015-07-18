package org.quinto.swing.table.model;

public interface IModelFieldGroup {
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
  
  IModelFieldGroup clone();
  boolean equals( Object obj, boolean withParent );
}
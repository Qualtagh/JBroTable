package org.quinto.swing.table.model;

import java.io.Serializable;

public class Triple< F, M, L > implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private F first;
  private M middle;
  private L last;

  public Triple( F first, M middle, L last ) {
    this.first = first;
    this.middle = middle;
    this.last = last;
  }

  public F getFirst() {
    return first;
  }

  public void setFirst( F first ) {
    this.first = first;
  }

  public M getMiddle() {
    return middle;
  }

  public void setMiddle( M middle ) {
    this.middle = middle;
  }

  public L getLast() {
    return last;
  }

  public void setLast( L last ) {
    this.last = last;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == this )
      return true;
    if ( !( obj instanceof Triple ) )
      return false;
    Triple o = ( Triple )obj;
    if ( this.first != o.first && ( this.first == null || !this.first.equals( o.first ) ) )
      return false;
    if ( this.middle != o.middle && ( this.middle == null || !this.middle.equals( o.middle ) ) )
      return false;
    if ( this.last != o.last && ( this.last == null || !this.last.equals( o.last ) ) )
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + ( this.first == null ? 0 : this.first.hashCode() );
    hash = 37 * hash + ( this.middle == null ? 0 : this.middle.hashCode() );
    hash = 37 * hash + ( this.last == null ? 0 : this.last.hashCode() );
    return hash;
  }

  @Override
  public String toString() {
    return "Triple: [ " + first + " | " + middle + " | " + last + " ]";
  }
}
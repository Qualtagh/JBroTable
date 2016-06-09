package org.quinto.swing.table.view;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

public class PredefinedRowSorter extends TableRowSorter< TableModel > {
  private static final Logger LOGGER = Logger.getLogger( PredefinedRowSorter.class );
  private static final SortKey EMPTY_ARRAY[] = new SortKey[ 0 ];
  
  private final JTable table;
  private SortKey preColumns[] = EMPTY_ARRAY;
  private SortKey postColumns[] = EMPTY_ARRAY;

  public PredefinedRowSorter( JTable table ) {
    super( table.getModel() );
    this.table = table;
  }
  
  private void check( SortKey modelColumns[], SortKey crossCheckColumns[], boolean post ) {
    TableModel tm = table.getModel();
    int max = tm.getColumnCount();
    Set< Integer > used = new HashSet< Integer >();
    for ( SortKey key : modelColumns ) {
      if ( key == null )
        throw new IllegalArgumentException( "SortKey must be non-null" );
      if ( key.getColumn() < 0 )
        throw new IllegalArgumentException( "SortKey column must be non-negative" );
      if ( key.getColumn() >= max )
        throw new IllegalArgumentException( "SortKey column is too high (out of model scope)" );
      if ( key.getSortOrder() == SortOrder.UNSORTED )
        throw new IllegalArgumentException( "SortKey must be ordered (ascending or descending)" );
      if ( !used.add( key.getColumn() ) )
        throw new IllegalArgumentException( "SortKey column must be unique (column " + key.getColumn() + " is repeating)" );
    }
    for ( SortKey key : crossCheckColumns )
      if ( used.contains( key.getColumn() ) )
        throw new IllegalArgumentException( "SortKey column must be unique (column " + key.getColumn() + " is already contained in " + ( post ? "post" : "pre" ) + " columns list)" );
  }
  
  public PredefinedRowSorter withPreColumns( SortKey... modelColumns ) {
    if ( modelColumns == null )
      modelColumns = EMPTY_ARRAY;
    if ( !Arrays.equals( preColumns, modelColumns ) ) {
      check( modelColumns, postColumns, true );
      preColumns = modelColumns;
      setSortKeys( getSortKeys() );
    }
    return this;
  }
  
  public PredefinedRowSorter withPostColumns( SortKey... modelColumns ) {
    if ( modelColumns == null )
      modelColumns = EMPTY_ARRAY;
    if ( !Arrays.equals( postColumns, modelColumns ) ) {
      check( modelColumns, preColumns, false );
      postColumns = modelColumns;
      setSortKeys( getSortKeys() );
    }
    return this;
  }

  public JTable getTable() {
    return table;
  }

  public SortKey[] getPreColumns() {
    return preColumns.length == 0 ? preColumns : preColumns.clone();
  }

  public SortKey[] getPostColumns() {
    return postColumns.length == 0 ? postColumns : postColumns.clone();
  }
  
  private void setSortKeysInternal( List< ? extends SortKey > sortKeys ) {
    try {
      Field field = DefaultRowSorter.class.getDeclaredField( "sortKeys" );
      boolean accessible = field.isAccessible();
      if ( !accessible )
        field.setAccessible( true );
      field.set( this, sortKeys );
      if ( !accessible )
        field.setAccessible( false );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchFieldException e ) {
      LOGGER.error( null, e );
    } catch ( SecurityException e ) {
      LOGGER.error( null, e );
    }
  }
  
  private Object getViewToModelInternal() {
    try {
      Field field = DefaultRowSorter.class.getDeclaredField( "viewToModel" );
      boolean accessible = field.isAccessible();
      if ( !accessible )
        field.setAccessible( true );
      Object ret = field.get( this );
      if ( !accessible )
        field.setAccessible( false );
      return ret;
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchFieldException e ) {
      LOGGER.error( null, e );
    } catch ( SecurityException e ) {
      LOGGER.error( null, e );
    }
    return null;
  }
  
  private void sortExistingDataInternal() {
    try {
      Method method = DefaultRowSorter.class.getDeclaredMethod( "sortExistingData" );
      boolean accessible = method.isAccessible();
      if ( !accessible )
        method.setAccessible( true );
      method.invoke( this );
      if ( !accessible )
        method.setAccessible( false );
    } catch ( IllegalAccessException e ) {
      LOGGER.error( null, e );
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( null, e );
    } catch ( NoSuchMethodException e ) {
      LOGGER.error( null, e );
    } catch ( InvocationTargetException e ) {
      LOGGER.error( null, e );
      LOGGER.error( null, ( ( InvocationTargetException )e ).getCause() );
    }
  }

  @Override
  public void setSortKeys( List< ? extends SortKey > sortKeys ) {
    List< ? extends SortKey > oldSortKeys = getSortKeys();
    List< ? extends SortKey > newSortKeys;
    if ( sortKeys != null && !sortKeys.isEmpty() ) {
      int max = getModelWrapper().getColumnCount();
      for ( SortKey key : sortKeys )
        if ( key == null || key.getColumn() < 0 || key.getColumn() >= max )
          throw new IllegalArgumentException( "Invalid SortKey" );
      newSortKeys = Collections.unmodifiableList( new ArrayList< SortKey >( sortKeys ) );
    } else
      newSortKeys = Collections.emptyList();
    setSortKeysInternal( newSortKeys );
    if ( !newSortKeys.equals( oldSortKeys ) ) {
      fireSortOrderChanged();
      boolean wasChanged = false;
      if ( preColumns.length > 0 || postColumns.length > 0 ) {
        List< SortKey > editableSortKeys = new ArrayList< SortKey >( newSortKeys );
        for ( int i = preColumns.length - 1; i >= 0; i-- ) {
          int modelColumn = preColumns[ i ].getColumn();
          int idx = indexOfColumn( editableSortKeys, preColumns.length - i - 1, editableSortKeys.size(), modelColumn );
          SortOrder sortOrder = idx < 0 ? preColumns[ i ].getSortOrder() : editableSortKeys.remove( idx ).getSortOrder();
          editableSortKeys.add( 0, new SortKey( modelColumn, sortOrder ) );
        }
        int to = editableSortKeys.size();
        for ( SortKey postColumn : postColumns ) {
          int modelColumn = postColumn.getColumn();
          int idx = indexOfColumn( editableSortKeys, preColumns.length, to, modelColumn );
          SortOrder sortOrder;
          if ( idx < 0 )
            sortOrder = postColumn.getSortOrder();
          else {
            sortOrder = editableSortKeys.remove( idx ).getSortOrder();
            to--;
          }
          editableSortKeys.add( new SortKey( modelColumn, sortOrder ) );
        }
        if ( wasChanged = !editableSortKeys.equals( newSortKeys ) )
          setSortKeysInternal( editableSortKeys );
      }
      if ( getViewToModelInternal() == null )
        sort();
      else
        sortExistingDataInternal();
      if ( wasChanged )
        setSortKeysInternal( newSortKeys );
    }
  }

  private int indexOfColumn( List< SortKey > sortKeys, int fromIncl, int toExcl, int column ) {
    for ( int i = toExcl - 1; i >= fromIncl; i-- )
      if ( sortKeys.get( i ).getColumn() == column )
        return i;
    return -1;
  }
};
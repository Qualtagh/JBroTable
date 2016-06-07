package org.quinto.swing.table.view;

import java.awt.Component;
import javax.swing.JTable;
import org.quinto.swing.table.model.IModelFieldGroup;

public interface CustomTableHeaderRenderer {
  /**
   * Warning: originalComponent may be shared within a level by different cells.
   * If rendering of one cell changes originalComponent's properties
   * then rendering of other cells should restore original properties (or set them to appropriate values).
   * @param originalComponent
   * @param table
   * @param value
   * @param isSelected
   * @param hasFocus
   * @param isDragged
   * @param row
   * @param viewColumn
   * @param modelColumn
   * @param dataField
   * @return 
   */
  public Component getTableCellRendererComponent( Component originalComponent, JTable table, Object value, boolean isSelected, boolean hasFocus, boolean isDragged, int row, int viewColumn, int modelColumn, IModelFieldGroup dataField );
}
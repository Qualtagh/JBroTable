package org.quinto.swing.table.view;

import java.awt.Component;
import org.quinto.swing.table.model.IModelFieldGroup;

public interface CustomTableHeaderRenderer {
  /**
   * <b>Warning:</b> originalComponent may be shared within a level by different cells.
   * If rendering of one cell changes originalComponent's properties
   * then rendering of other cells should restore original properties (or set them to appropriate values).
   * @param originalComponent a component returned by an underlying (L&amp;F) renderer
   * @param table a corresponding table
   * @param value table header cell value
   * @param isSelected is mouse over this cell
   * @param hasFocus was a focus gained by this cell (e. g., by pressing tab)
   * @param isDragged is mouse dragging this cell (column or column group)
   * @param row a row (level) in this table header
   * @param viewColumn cell horizontal position in the current row as it is viewed at the moment
   * @param modelColumn cell horizontal position in the current row as it was defined by the model at initialization
   * @param dataField model column (or column group) that corresponds to this cell
   * @return a component that defines rendering of the current table header cell
   */
  public Component getTableCellRendererComponent( Component originalComponent, JBroTable table, Object value, boolean isSelected, boolean hasFocus, boolean isDragged, int row, int viewColumn, int modelColumn, IModelFieldGroup dataField );
}
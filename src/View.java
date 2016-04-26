import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;

public class View implements ChangeListener
{
	private Model model;
	private JPanel hierarchyPanel;
	private JTable table;
	private Boolean deleteEnabled = false;
	private String deleteButtonText = "Deletion of Columns = " + deleteEnabled;
	private Boolean diceEnabled = false;
	private String diceButtonText = "Dice of Columns = " + diceEnabled;
	private ArrayList<String> diceSelections = new ArrayList<String>();
	private int diceCol = 0;

	public View(Model m)
	{
		model = m;

		setTable();
	}

	public void setTable()
	{
		String[] dimensions = { "Store", "Product", "Time", "Promotion" };
		hierarchyPanel = new JPanel();
		hierarchyPanel
				.setLayout(new BoxLayout(hierarchyPanel, BoxLayout.Y_AXIS));

		for (int i = 0; i < model.hierarchy.size(); i++)
		{
			JPanel temp = new JPanel(new FlowLayout());
			for (String s : model.hierarchy.get(i))
			{
				temp.add(createLabel(s));
				temp.add(new JLabel(" ----> "));
			}
			temp.remove(temp.getComponentCount() - 1);
			hierarchyPanel.add(new JLabel(dimensions[i]));
			hierarchyPanel.add(temp);
		}

		final JButton diceButton = new JButton(diceButtonText);
		diceButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (diceEnabled && !diceSelections.isEmpty())
				{
					// set model.c, drilldown and model.update
					String selectedColumn = (String) model.columnNames[diceCol];
					String child = findChild(selectedColumn);
					String[] a = model.a;
					if (!child.isEmpty())
					{
						for (int i = 0; i < a.length; i++)
						{
							if (a[i].equalsIgnoreCase(selectedColumn))
							{
								a[i] = child;
							}
						}
					}
					String c = model.c;
					if (!c.contains("WHERE"))
						c = "WHERE (";
					else
						c += "AND ( ";
					for (String s : diceSelections)
					{
						System.out.println(s);
						c += selectedColumn + " = '" + s + "' OR ";
					}
					c.trim();
					c = c.substring(0, c.length() - 3) + " ) ";
					model.update(a, model.m, c);
					diceSelections = new ArrayList<String>();
				}
				diceEnabled = !diceEnabled;
				diceButtonText = "Dice of Columns = " + diceEnabled;
				model.update();// updates button only
			}
		});
		hierarchyPanel.add(diceButton);

		final JButton deleteButton = new JButton(deleteButtonText);
		deleteButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				deleteEnabled = !deleteEnabled;
				deleteButtonText = "Deletion of Columns = " + deleteEnabled;
				model.update();
			}
		});
		hierarchyPanel.add(deleteButton);

		table = new JTable(model.rowData, model.columnNames);
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				String selectedData = (String) table.getValueAt(row, col);
				if (diceEnabled) // allow multiple selections for WHERE clause
				{
					boolean selectedAlready = false;
					for (String s : diceSelections)
					{
						if (s.equalsIgnoreCase(selectedData))
							selectedAlready = true;
					}
					if (!selectedAlready)
						diceSelections.add(selectedData);
					diceCol = col;
				} else if (col >= 0)
				{
					drillDown(col, selectedData);

					model.update(model.a, model.m, model.c);
				}
			}
		});
		table.setCellSelectionEnabled(true);

		final JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				int col = header.columnAtPoint(e.getPoint());
				String selected = (String) model.columnNames[col];
				if (selected.equals(model.m))
				{
					model.measureIndex++;
					if (model.measureIndex >= model.measures.length)
						model.measureIndex = 0;
					model.update(model.a, model.measures[model.measureIndex],
							model.c);
				} else if (deleteEnabled)
				{
					String[] a = model.a;
					String[] newA;
					if (model.a.length > 0)
					{
						newA = new String[model.a.length - 1];
						int j = 0;
						for (int i = 0; i < a.length; i++, j++)
						{
							if (a[i].equalsIgnoreCase(selected))
								j--;
							else
								newA[j] = a[i];
						}
					} else
						newA = new String[] {};
					model.update(newA, model.m, model.c);
				} else
					rollUp(col, selected);
			}
		});
	}

	protected void rollUp(int col, String selected)
	{

		String parent = findParent(selected);

		String[] a = model.a;
		String c = model.c;
		if (!parent.isEmpty())
		{
			for (int i = 0; i < a.length; i++)
			{
				if (a[i].equalsIgnoreCase(selected))
				{
					a[i] = parent;
					if (c.contains(parent))
					{
						int start = c.indexOf(parent);
						String st = c.substring(0, start);
						if (st.trim().endsWith("AND"))
						{
							int tart = st.lastIndexOf("AND");
							st = st.substring(0, tart);
						}
						String et = c.substring(start);
						if (et.contains("AND"))
						{
							int star = et.indexOf("AND");
							et = et.substring(star);
						} else
							et = "";
						if (st.trim().endsWith("WHERE ("))
							st = "WHERE ";
						if (st.equals("WHERE ") && et.startsWith("AND"))
							et = et.substring(3);
						c = st + et;
						if (c.contains("WHERE AND"))
							c = "WHERE" + c.substring(9);

					}
					if (c.trim().endsWith("WHERE")
							|| c.trim().endsWith("WHERE ("))
						c = "";
					else if (c.trim().endsWith("AND")
							|| c.trim().endsWith("AND ("))
					{
						int start = c.lastIndexOf("AND");
						c = c.substring(0, start);
					}
					break;
				}
			}
		}
		model.update(a, model.m, c);
	}

	protected void drillDown(int col, String selectedData)
	{
		String selectedColumn = (String) model.columnNames[col];
		String child = findChild(selectedColumn);
		if (!child.isEmpty())
		{
			String[] a = model.a;
			String c = model.c;
			for (int i = 0; i < a.length; i++)
			{
				if (a[i].equalsIgnoreCase(selectedColumn))
				{
					a[i] = child;
					if (c.contains("WHERE"))
					{
						c += " AND " + selectedColumn + " = '" + selectedData
								+ "'";
					} else
					{
						c += "WHERE " + selectedColumn + " = '" + selectedData
								+ "'";
					}
					break;
				}
			}
			model.update(a, model.m, c);
		}
	}

	private String findParent(String selected)
	{
		String parent = "";
		boolean done = false;
		for (int i = 0; i < model.hierarchy.size(); i++)
		{
			for (int j = 0; j < model.hierarchy.get(i).length; j++)
			{
				done = model.hierarchy.get(i)[j].equals(selected);
				if (done)
					break;
				parent = model.hierarchy.get(i)[j];
			}
			if (done)
				break;
			parent = "";
		}
		return parent;
	}

	private String findChild(String selected)
	{
		String child = "";
		boolean done = false;
		for (int i = 0; i < model.hierarchy.size(); i++)
		{
			for (int j = model.hierarchy.get(i).length - 1; j >= 0; j--)
			{
				done = model.hierarchy.get(i)[j].equals(selected);
				if (done)
					break;
				child = model.hierarchy.get(i)[j];
			}
			if (done)
				break;
			child = "";
		}
		return child;
	}

	public JLabel createLabel(String text)
	{
		final JLabel label = new JLabel(text);
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				String selected = label.getText();
				// if (findParent(selected).isEmpty())
				{
					boolean exists = false;
					boolean inDimension = false;
					boolean selectionInA = false;
					String attributeInSelectedDimensionAndInTable = "";
					for (String s : model.a)
					{
						for (String[] t : model.hierarchy)
						{
							// finds dimension containing selection
							for (int i = 0; i < t.length; i++)
							{
								if (t[i].equalsIgnoreCase(selected))
								{
									inDimension = true;
									break;
								}
							}
							if (inDimension)
							{
								inDimension = false;
								// checks if an attribute of the dimension
								// exists in a (the table columns)
								for (int i = 0; i < t.length; i++)
								{
									if (t[i].equalsIgnoreCase(s))
									{
										exists = true; // t[i]/s is an attribute
														// in a.
										attributeInSelectedDimensionAndInTable = s;
										break;
									}
								}
								break;
							}
						}

						if (s.equalsIgnoreCase(selected))
						{
							exists = true;
							selectionInA = true;
						}
						if (exists)
							break;
					}
					if (!exists)
					{
						String[] a = new String[model.a.length + 1];
						a[0] = selected;
						for (int i = 0; i < model.a.length; i++)
							a[i + 1] = model.a[i];
						model.update(a, model.m, "");// keep constraints
					} else if (!selectionInA)
					{
						// drilldown/rollup based on a.i.s.d.a.i.t and selected
						// relationship
						String[] a = model.a;
						for (int i = 0; i < a.length; i++)
						{
							if (a[i].equalsIgnoreCase(attributeInSelectedDimensionAndInTable))
							{
								a[i] = selected;
								break;
							}
						}
						model.update(a, model.m, "");// keepconstraints
					}
				}
			}
		});
		return label;
	}

	public JPanel getHierarchyPanel()
	{
		return hierarchyPanel;
	}

	public JTable getTable()
	{
		return table;
	}

	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		setTable();
	}

}

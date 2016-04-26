import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Controller implements ChangeListener
{
	private View view;
	private JFrame frame;
	private Component comp, comp1;

	public Controller(View v)
	{
		frame = new JFrame();
		view = v;
	}

	public void setFrame()
	{

		comp = new JScrollPane(view.getTable());
		comp1 = new JScrollPane(view.getHierarchyPanel());
		frame.setLayout(new BorderLayout());
		frame.add(comp, BorderLayout.CENTER);
		frame.add(comp1, BorderLayout.WEST);
	}

	public void startFrame()
	{
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		frame.remove(comp);
		frame.remove(comp1);
		setFrame();
		startFrame();
	}

}

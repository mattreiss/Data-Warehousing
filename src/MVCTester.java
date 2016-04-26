
public class MVCTester
{
	private static Model model;

	public static void main(String[] args)
	{

		// initialize classes
		model = new Model();
		View v = new View(model);
		Controller c = new Controller(v);
		model.attach(v);
		model.attach(c);

		c.setFrame();
		c.startFrame();
	}}

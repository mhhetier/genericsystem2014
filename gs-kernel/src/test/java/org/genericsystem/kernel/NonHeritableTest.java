package org.genericsystem.kernel;

import java.util.Objects;

import org.testng.annotations.Test;

@Test
public class NonHeritableTest extends AbstractTest {

	public void test_enable() {
		Root engine = new Root();
		Generic tree = engine.addInstance("Tree");
		tree.enableHeritable();
		assert tree.isHeritableEnabled();
	}

	public void test_enableByDefault() {
		Root engine = new Root();
		Generic tree = engine.addInstance("Tree");
		assert tree.isHeritableEnabled();
	}

	public void test_disable() {
		Root engine = new Root();
		Generic tree = engine.addInstance("Tree");
		tree.disableHeritable();
		assert !tree.isHeritableEnabled();
	}

	public void test_disable_then_enable() {
		Root engine = new Root();
		Generic tree = engine.addInstance("Tree");
		assert tree.isHeritableEnabled();
		tree.disableHeritable();
		assert !tree.isHeritableEnabled();
		tree.enableHeritable();
		assert tree.isHeritableEnabled();
		tree.disableHeritable();
	}

	public void test_enable_then_disable() {
		Root engine = new Root();
		Generic tree = engine.addInstance("Tree");
		tree.enableHeritable();
		assert tree.isHeritableEnabled();
		tree.disableHeritable();
		assert !tree.isHeritableEnabled();
	}

	public void test_tree_color() {
		Root engine = new Root();

		Generic tree = engine.addInstance("Tree");
		Generic color = engine.addInstance("Color");
		Generic treeColor = tree.addAttribute("TreeColor", color);

		treeColor.disableHeritable();

		Generic blue = color.addInstance("blue");
		Generic red = color.addInstance("red");
		Generic green = color.addInstance("green");

		tree.setHolder(treeColor, "treeIsBlueByDefault", blue);

		Generic html = tree.addInstance("html");
		Generic head = tree.addInstance(html, "head");
		Generic body = tree.addInstance(html, "body");
		Generic div = tree.addInstance(body, "div");

		html.setHolder(treeColor, "htmlIsRed", red);
		div.setHolder(treeColor, "divIsGreen", green);

		assert Objects.equals(tree.getHolders(treeColor).first().getTargetComponent(), blue);
		assert Objects.equals(head.getHolders(treeColor).first(), null);
		assert Objects.equals(body.getHolders(treeColor).first(), null);
		assert Objects.equals(div.getHolders(treeColor).first().getTargetComponent(), green);
	}
}
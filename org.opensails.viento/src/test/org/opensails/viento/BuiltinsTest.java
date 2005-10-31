package org.opensails.viento;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class BuiltinsTest extends TestCase {
    Binding binding = new Binding();

    public void testIf() throws Exception {
        verifyRender("$if(true)[[here]]", "here");
        verifyRender("$if(false)[[here]]", "");
        verifyRender("$if(true)[[here]].else[[there]]", "here");
        verifyRender("$if(false)[[here]].else[[there]]", "there");
        verifyRender("$if(true)[[here]].elseif(false)[[there]]", "here");
        verifyRender("$if(false)[[here]].elseif(true)[[there]]", "there");
        verifyRender("$if(false)[[here]].elseif(false)[[there]]", "");
        verifyRender("$if(false)[[here]].elseif(true)[[there]].else[[nowhere]]", "there");
        verifyRender("$if(false)[[here]].elseif(false)[[there]].else[[nowhere]]", "nowhere");
        verifyRender("$if(true)[[here]].elseif(true)[[there]].else[[nowhere]]", "here");

        verifyRender("$if('asdf')[[here]]", "here");
        verifyRender("$if(null)[[here]]", "");

        // just for fun
        verifyRender("$set(joe)[[here]]$if(true, $joe)", "here");
    }

    public void testEach() throws Exception {
        binding.put("list", Arrays.asList(new String[] { "one", "two", "three" }));
        verifyRender("$list.each(item)[[<h1>$item</h1>]]", "<h1>one</h1><h1>two</h1><h1>three</h1>");
        verifyRender("$list.each(item)[[<h$index;>$item</h$index;>]]", "<h1>one</h1><h2>two</h2><h3>three</h3>");
        verifyRender("$list.each(item, indexB)[[<h$indexB;>$item</h$indexB;>]]", "<h1>one</h1><h2>two</h2><h3>three</h3>");

        try {
			verifyRender("$!list.each(item)[[<h1>$notHere</h1>]]", "");
			fail("! shouldn't silence things inside the block");
		} catch (Exception expected) {
		}
        
        verifyRender("$list.each(item)[[<h1>$item</h1>]].sans(one)", "<h1>two</h1><h1>three</h1>");
        verifyRender("$list.each(item)[[<h1>$item</h1>]].sans([one, three])", "<h1>two</h1>");
        verifyRender("$list.each(item)[[<h1>$item</h1>]].sans({length: 3})", "<h1>three</h1>");
        verifyRender("$list.each(item)[[<h1>$item</h1>]].sans({length: [3, 5]})", "");
        verifyRender("$list.before[[before]].each(item)[[<h1>$item</h1>]].sans({length: [3, 5]})", "");
        
        verifyRender("$set(list, [])$list.before[[before ]].each(each)[[$each]].delimiter[[, ]].after[[ after]]", "");
        verifyRender("$set(list, [one])$list.before[[before ]].each(each)[[$each]].delimiter[[, ]].after[[ after]]", "before one after");
        verifyRender("$set(list, [one, two])$list.before[[before ]].each(each)[[$each]].delimiter[[, ]].after[[ after]]", "before one, two after");
        verifyRender("$set(list, [one, two])$list.before[[before ]].each(each)[[$each]].delimiter(', ').after[[ after]]", "before one, two after");

        binding.put("array", new String[] { "one", "two", "three" });
        verifyRender("$array.each(item)[[<h1>$item</h1>]]", "<h1>one</h1><h1>two</h1><h1>three</h1>");
    }
    
    public void testSilence() throws Exception {
        verifyRender("$![[asdf$notHere]]", "");

        binding.put("key", "value");
        verifyRender("$![[asdf$key]]", "asdfvalue");

        verifyRender("$![[asdf$notHere]].?[[not here]]", "not here");

        binding.setExceptionHandler(new ExceptionHandler() {
			public Object resolutionFailed(String methodName, Object[] args, List<Throwable> failedAttempts) {
				return "here";
			}

			public Object resolutionFailed(Object target, String methodName, Object[] args, List<Throwable> failedAttempts) {
				return "here";
			}
        });
        verifyRender("$![[$notHere]]", "");
    }
    
    public void testWith() throws Exception {
    	binding.put("string", "string");
		verifyRender("$with($string)[[$length]]", "6");
	}
    
    public void testSet() throws Exception {
    	verifyRender("$set(string, 'string')$string", "string");
    	// Blocks just work. I love this stuff.
    	verifyRender("$set(name, 'Fred')$set(greeting)[[Welcome $name;!]]$greeting", "Welcome Fred!");
    }

    public void testEscape() throws Exception {
        verifyRender("$$", "$");
        verifyRender("$escape(']]')", "]]");
        verifyRender("$escape('##')", "##");
    }
    
    public void testProperties() throws Exception {
		binding.put("bean", new Bean());
		verifyRender("$bean.properties.each(property)[[<p>$property.name: $property.value</p>]]", "<p>one: 1</p><p>two: 2</p>");
	}

    protected void verifyRender(String input, String output) {
        VientoTemplate template = new VientoTemplate(input);
        assertEquals(output, template.render(binding));
    }
    
    public class Bean {
    	public int getOne() {
    		return 1;
    	}
    	
    	public int getTwo() {
    		return 2;
    	}
    }
}
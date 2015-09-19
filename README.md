# Oak
## Better than JSON, much better than XML

Oak is a library that supports an alternative to XML and JSON for files to store and transport data.

Why use Oak instead of JSON or XML? For these reasons:

1. Oak is more expressive and easier to write and read.
2. You can build your own 'dialects' rather like a domain specific language. Oak can then do the hard work of checking the correctness of the input and producing meaningful messages.
3. You can have Oak automatically construct an object hierarchy from a data file in a single like of code.

See [Oak's home page](http://www.inexas.org/display/inexas/Products+-+Oak) for more info. Contact me if you need help.

Here's an example Oak file for you to chew on...

```
/**
 * Example Oak file
 */
Person {
	first: "John";
	last: "Smith";
	dob: @1991/3/14;
	creditRating: 7.2;
	Address [{
		line1: "461 Ocean Boulavard";
		city: "TUCSON";
		state: "AZ";
		zip: "85705";
		country: "USA";
	}, {
		line1: "Kappelergasse 1";
		city: "Zürich";
		state: "8022";
		zip: "8022";
		country: "Switzerland";
	}]
}
```

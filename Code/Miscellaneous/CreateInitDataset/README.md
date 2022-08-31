1. Clone the Java repositories into a *root* folder
2.  Use [srcML] to create for each repository a xml file representing all the code
3. Pass the path to the *root* folder to `create_init_java_methods_ds.py`
4. Use `clean_java_methods_ds.py` on the file produced in previous step to clean the dataset
5. Use `Extractor.java` to detect all the methods' log statements

Note: The CSV file produced by `Extractor.java` still need instances containing "custom" log levels to be filtered out
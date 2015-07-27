package daikon.split;

import daikon.inv.*;
import java.io.*;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signature.qual.*;
import org.checkerframework.dataflow.qual.*;
import typequals.*;
*/

/**
 * A SplitterObject is the starting point for all the information we
 * have about a splitting condition. It is created immediately when
 * the condition is read from the .spinfo file, and later contains a
 * reference to the compiled "Splitter" object.
 **/
public class SplitterObject implements Comparable<SplitterObject> {

  private /*@MonotonicNonNull*/ Splitter splitter;
  private String condition; // the condition
  private /*@BinaryName*/ String className = "Unassigned"; // the Java classname of this Splitter
  private String directory; // the directory where it resides
  private String pptName; // the program point with which it is associated
  private boolean exists = false;
  private String testString = "Unassigned";
  // Not necessarily an error message -- really just a status message.
  private String errorMessage = "Splitter not yet loaded";
  private int guid = -999;      // -999 indicates not yet set
  private /*@MonotonicNonNull*/ File classFile; // class file containing compiled code for this splitter

  public boolean dummyDesired = false;
  public /*@Nullable*/ String daikonFormat   = null;
  public /*@Nullable*/ String javaFormat     = null;
  public /*@Nullable*/ String escFormat      = null;
  public /*@Nullable*/ String simplifyFormat = null;
  public /*@Nullable*/ String jmlFormat      = null;
  public /*@Nullable*/ String dbcFormat      = null;
  public /*@Nullable*/ String csharpFormat   = null;

  /**
   * @param condition The splitting condition of this splitter
   * @param directory The directory where the source of this splitter is located.
   */
  public SplitterObject (String pptName, String condition, String directory) {
    this.condition = condition;
    this.pptName = pptName;
    this.directory = directory;
    this.javaFormat = condition;
    this.daikonFormat = condition;
    this.csharpFormat = condition;
  }

  /**
   * Sets the "splitter" field of this object to a newly-instantiated object.
   * @param loader The SplitterLoader used to load the compiled source.
   * Must not be null.
   */
  public void load (SplitterLoader loader) {
    Class<?> tempClass = loader.load_Class(className, directory + className + ".class");
    if (tempClass != null) {
      try {
        splitter = (Splitter) tempClass.newInstance();
      } catch (ClassFormatError ce) {
        ce.printStackTrace(System.out);
        throw new Error(ce);
      } catch (InstantiationException ie) {
        ie.printStackTrace(System.out);
        throw new Error(ie);
      } catch (IllegalAccessException iae) {
        iae.printStackTrace(System.out);
        throw new Error(iae);
      }
      DummyInvariant dummy = new /*@Prototype*/ DummyInvariant(
         daikonFormat, javaFormat, escFormat, simplifyFormat,
                       jmlFormat, dbcFormat, csharpFormat, dummyDesired);
      splitter.makeDummyInvariantFactory(dummy);
      errorMessage = "Splitter exists " + this.toString();
      exists = true;
    } else {
      errorMessage = "\rNo class data for " + this.toString() + ", to be loaded from " + directory + className + ".class";
      exists = false;
    }
  }

  /**
   * @return true if the Splitter object exists for this
   * SplitterObject, i.e. whether it successfully loaded.
   */
  public boolean splitterExists() {
    return exists;
  }

  /**
   * @return true if the .class file exists for the Splitter
   * represented by this SplitterObject, false otherwise
   */
  public boolean compiled () {
    if (classFile != null && classFile.exists()) {
      errorMessage = "Splitter exists " + this.toString();
      return true;
    }
    return false;
  }

  /**
   * @return the Splitter that this SplitterObject represents. Null if
   * splitterExists() == false
   */
  public /*@Nullable*/ Splitter getSplitter() {
    return this.splitter;
  }

  /**
   * set the error message of this this SplitterObject. This indicates the status of
   * the Splitter.
   */
  public void setError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * get the error message of this SplitterObject.
   */
  public String getError () {
    return this.errorMessage;
  }

  /**
   * Set the unique ID of this splitterObject.
   */
  public void setGUID(int ID) {
    this.guid = ID;
  }

  /**
   * Return the unique ID of this splitterObject.
   */
  public int getGUID( ) {
    return this.guid;
  }

  /**
   * @return the full source of the Splitter.
   */
  public String getFullSourcePath () {
    return (directory + className + ".java");
  }

  /**
   * @return the program point represented by this Splitter.
   */
  public String getPptName () {
    return this.pptName;
  }

  /**
   * Set the className of this Splitter.
   */
  public void setClassName(/*@BinaryName*/ String className) {
    this.className = className;
    classFile = new File(directory + className + ".class");
  }

  /**
   * @return the className of the Splitter
   */
  public /*@BinaryName*/ String getClassName() {
    return this.className;
  }

  public void setDirectory (String directory) {
    this.directory = directory;
  }

  public String getDirectory () {
    return this.directory;
  }

  /**
   * @return the condition represented by the Splitter
   */
  public String condition () {
    return this.condition;
  }

  public void setTestString (String testString) {
    this.testString = testString;
  }

  public String getTestString() {
    return this.testString;
  }

  /*@SideEffectFree*/ public String toString() {
    return (className + ": " + "condition: " + condition + ", testString: " + testString
            + ", @ " + pptName);
  }

  /*@Pure*/ public int compareTo(SplitterObject o) {
    return this.guid - o.getGUID();
  }
}

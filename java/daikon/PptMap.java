package daikon;

import java.io.*;
import java.util.*;
import utilMDE.*;

/** Maps from a name (a String) to a PptTopLevel. */
public class PptMap
  implements Serializable
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20040921L;

  private final Map nameToPpt = new HashMap();

  public void add(PptTopLevel ppt) {
    nameToPpt.put(ppt.name(), ppt);
  }

  public void addAll(List ppts) {
    for (Iterator iPpt = ppts.iterator(); iPpt.hasNext(); ) {
      PptTopLevel ppt = (PptTopLevel) iPpt.next();
      add (ppt);
    }
  }

  public PptTopLevel get(String name) {
    return (PptTopLevel) nameToPpt.get(name);
  }

  public PptTopLevel get(PptName name) {
    return get(name.toString());
  }

  public boolean containsName(String name) {
    return nameToPpt.containsKey(name);
  }

  /**
   * @return unstably-ordered collection of PptTopLevels
   * @see #pptIterator()
   **/
  public Collection asCollection() {
    return Collections.unmodifiableCollection(nameToPpt.values());
  }

  public Collection nameStringSet() {
    return Collections.unmodifiableSet(nameToPpt.keySet());
  }

  /**
   * @return an iterator over the PptTopLevels in this, sorted by
   * Ppt.NameComparator on their names.  This is good for consistency.
   **/
  public Iterator pptIterator() {
    TreeSet sorted = new TreeSet(new Ppt.NameComparator());
    sorted.addAll(nameToPpt.values());
    // Use a (live) view iterator to get concurrent modification
    // exceptions, and an iterator over sorted to get consistency.
    final Iterator iter_view = nameToPpt.values().iterator();
    final Iterator iter_sort = sorted.iterator();
    return new Iterator() {
        public boolean hasNext() {
          boolean result = iter_view.hasNext();
          Assert.assertTrue(result == iter_sort.hasNext());
          return result;
        }
        public Object next() {
          iter_view.next(); // to check for concurrent modifications
          return iter_sort.next();
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }

  /**
   * @return an iterator over the PptTopLevels in this, sorted by
   * Ppt.NameComparator on their names.  This differs from pptIterator()
   * in that it includes all ppts (including conditional ppts).
   **/
  public Iterator ppt_all_iterator() {
    TreeSet sorted = new TreeSet(new Ppt.NameComparator());
    sorted.addAll(nameToPpt.values());
    // Use a (live) view iterator to get concurrent modification
    // exceptions, and an iterator over sorted to get consistency.
    final Iterator iter_view = nameToPpt.values().iterator();
    final Iterator iter_sort = sorted.iterator();
    return new Iterator() {
        Iterator cond_iterator = null;
        public boolean hasNext() {
          if ((cond_iterator != null) && cond_iterator.hasNext())
            return (true);
          boolean result = iter_view.hasNext();
          Assert.assertTrue(result == iter_sort.hasNext());
          return result;
        }
        public Object next() {
          if ((cond_iterator != null) && cond_iterator.hasNext())
            return (cond_iterator.next());
          iter_view.next(); // to check for concurrent modifications
          PptTopLevel ppt = (PptTopLevel) iter_sort.next();
          if ((ppt != null) && ppt.has_splitters())
            cond_iterator = ppt.cond_iterator();
          return (ppt);
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
  }

  /** Iterate over the PptTopLevels and trim them. */
  public void trimToSize() {
    for (Iterator i = nameToPpt.values().iterator(); i.hasNext(); ) {
      PptTopLevel ppt = (PptTopLevel) i.next();
      ppt.trimToSize();
    }
  }

  /**
   * Check the rep invariant of this.  Throws an Error if incorrect.
   **/
  public void repCheck() {
    for (Iterator i = this.pptIterator(); i.hasNext(); ) {
      PptTopLevel ppt = (PptTopLevel) i.next();
      ppt.repCheck();
    }
  }

  /**
   * Return the number of active PptSlices.
   **/
  public int countSlices() {
    int result = 0;
    for (Iterator i = this.pptIterator(); i.hasNext(); ) {
      PptTopLevel ppt = (PptTopLevel) i.next();
      result += ppt.numViews();
    }
    return result;
  }

  public int size() {
    return nameToPpt.size();
  }

  public String toString() {
    return "PptMap: " + nameToPpt.toString();
  }

  /**
   * Blow away any PptTopLevels that never saw any samples (to reclaim space).
   **/
  public void removeUnsampled() {
    Iterator iter = nameToPpt.values().iterator();
    while (iter.hasNext()) {
      PptTopLevel ppt = (PptTopLevel)iter.next();
      if (ppt.num_samples() == 0)
        iter.remove();
    }
  }
}

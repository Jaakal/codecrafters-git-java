import java.util.ArrayList;
import java.util.Iterator;

public class GitRefs {
  private ArrayList<GitRef> refs;
  private ArrayList<String> capabilities;

  public GitRefs(ArrayList<GitRef> refs, ArrayList<String> capabilities) {
    this.refs = refs;
    this.capabilities = capabilities;
  }

  public ArrayList<GitRef> getRefs() {
    return this.refs;
  }

  public ArrayList<String> getCapabilities() {
    return this.capabilities;
  }

  public String getHeadRefHash() {
    Iterator<GitRef> it = this.refs.iterator();
    while (it.hasNext()) {
      GitRef gitRef = it.next();

      if (gitRef.getName().equals("HEAD")) {
        return gitRef.getHash();
      }
    }

    return null;
  }

  public void print() {
    Console.printYellow("Refs:");
    this.refs.stream().forEach((ref) -> {
      System.out.println(ref.getHash() + " " + ref.getName());
    });
    System.out.println();
    Console.printYellow("Capabilities:");
    System.out.println(this.capabilities.toString());
  }
}

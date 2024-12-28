public final class ASCII {
  private ASCII() {
  }

  public static final int NULL = 0;
  public static final int LINE_FEED = 10;
  public static final int BLANK_SPACE = 32;

  public static int COMMIT_COUNTER = 0;
  public static int TREE_COUNTER = 0;
  public static int BLOB_COUNTER = 0;
  public static int TAG_COUNTER = 0;
  public static int OFS_DELTA_COUNTER = 0;
  public static int REF_DELTA_COUNTER = 0;
}

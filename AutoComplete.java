/**
 * An implementation of the AutoCompleteInterface using a DLB Trie.
 */

 import java.util.ArrayList;

 public class AutoComplete implements AutoCompleteInterface {

  private DLBNode root;
  private StringBuilder currentPrefix;
  private DLBNode currentNode;
  //TODO: Add more instance variables as needed
  private DLBNode cur;
  private DLBNode pre;
  private StringBuilder prefixPrediction;
  private boolean newWord = false;
  private int numSeperateOut = 0;

  public AutoComplete(){
    root = null;
    currentPrefix = new StringBuilder();
    currentNode = null;
  }

  /**
   * Adds a word to the dictionary in O(alphabet size*word.length()) time
   * @param word the String to be added to the dictionary
   * @return true if add is successful, false if word already exists
   * @throws IllegalArgumentException if word is the empty string
   */
    public boolean add(String word){
      //if the word is null, or if it is the empty string, throw exception.
      if (word.toString() == null) throw new IllegalArgumentException("calls put() with a null key");
      if (word.equals("")) throw new IllegalArgumentException("calls put() with an empty key");
      //check for duplicate by getting traversing the trie to see if the word passed in already exists. return false if it is
      if (root != null) {
        DLBNode duplicate = getNode(root, word.toString(), 0);
        if ((duplicate != null) && duplicate.isWord) {
          return false;
        }
      }
      int pos = 0; 
      int size = 0;
      boolean isWord = false;
      //call the helper add method to add
      root = put(root, word.toString(), pos, size, isWord);
      if (root == null) {
        return false;
      }
      return true;
    }

    private DLBNode put(DLBNode x, String key, int pos, int size, boolean isWord) {
      DLBNode result = x;
      //if node x is null
      if (x == null){
          //create new node
          result = new DLBNode(key.charAt(pos));
          //increment size
          result.size++;
          //if we are not at the end of the key
          if(pos < key.length()-1){
            //recursively call the method to add the next letter
            result.child = put(result.child, key, pos+1, size, isWord);
            //set child/parent link
            result.child.parent = result;
          } else {
            //if we are at the end of the key, it is a word, set isWord to true
            result.isWord = true;
          }
        //if the letter equals an existing letter
      } else if(x.data == key.charAt(pos)) {
          //if we are not at the end of the key
          if(pos < key.length()-1){
            //recursively call the method to add the next letter
            result.child = put(result.child, key, pos+1, size, isWord);
            //increment sizze
            result.size++;
            //set child/parent link
            result.child.parent = result;
            //if we are at the end of the key
          } else {
            //increment size
            result.size++;
            //if we are at the end of the key, it is a word, set isWord to true
            result.isWord = true;
          }
        //if it is not null and it does not equal x
      } else {
        //traverse through siblings by recursion
        result.nextSibling = put(result.nextSibling, key, pos, size, isWord);
        //set sblings link
        result.nextSibling.previousSibling = result; 
      }
      return result;
  }


  /**
   * appends the character c to the current prefix in O(alphabet size) time. 
   * This method doesn't modify the dictionary.
   * @param c: the character to append
   * @return true if the current prefix after appending c is a prefix to a word 
   * in the dictionary and false otherwise
   */
    public boolean advance(char c){
      //if it is a new word, and currentNode/currentPrefix is out of sync
      if (newWord) {
        //add to the currentPrefix
        currentPrefix.append(c);
        //increase the count of the letters out of sync by
        numSeperateOut++;
        //set newWord (out of sync Boolean) to true
        newWord = true;
        //return false since it is not a prefix to a word in the dictionary
        return false;
      }
      //if currentPrefix is null
      if (currentPrefix == null) {
        //initialize currentPrefix
        currentPrefix = new StringBuilder();
      }
      //if currentNode is null
      if (currentNode == null) {
        //innitialize currentNode
        currentNode = root;
      } 
      //go to the children of the last node investigated to start the search for the new char
      else {
        //if currentNode's child is not null
        if (currentNode.child != null) {
          //set currentNode to its child
          currentNode = currentNode.child;
        }
        else {
          //if the currentNode's child is null
          //add to the currentPrefix
          currentPrefix.append(c);
          //increase the count of the letters out of sync by
          numSeperateOut++;
          //set newWord (out of sync Boolean) to true
          newWord = true;
          //return false since it is not a prefix to a word in the dictionary
          return false;
        }
      }
      //if the char we are looking for is the currentNode
      if (currentNode.data == c) {
        //add the char to the current prefix
        currentPrefix.append(c);
        //return true since it is a prefix to a word in the dictionary
        return true;
      }
      //if the char we are looking for is not in the currentNode
      else {
        boolean success;
        //advance to its sibling(s) recursively 
        success = advanceSibling(c);
        //if the char is in a sibling
        if (success) {
          //return true since it is a prefix to a word in the dictionary
          return true;
          //else, it is a new word
        } else {
          //return currentNode to the last valid node
          retreatBackNode();
          //add the char to currentPrefix
          currentPrefix.append(c);
          //increase the count of the letters out of sync by
          numSeperateOut++;
          //set newWord (out of sync Boolean) to true
          newWord = true;
          //return false since it is not a prefix to a word in the dictionary
          return false;
        }
      }
    }

    private boolean advanceSibling(char c) {
      //make new cur node to simulate currentNode
      if (cur == null) {
        cur = currentNode;
      }
      //if the next sibling of cur is not null
      if (cur.nextSibling != null) {
        //go to next sibling
        cur = cur.nextSibling;
      }
      //if there isn't a next sibling
      else {
        //reset cur
        cur = null;
        //return false because we did not find a node that has the char
        return false;
      }
      //if the char we are looking for is in the node we are at
      if (cur.data == c) {
        //add char to currentPrefix
        currentPrefix.append(c);
        //currentNode will now be brought to cur, since it is where the char is at
        currentNode = cur; 
        //reset cur
        cur = null;
        //return true since it is a prefix to a word in the dictionary
        return true;
      } 
      //if the char we are looking for is not at the node we are at
      else {
        boolean success;
        //go to the next sibling recursively
        success = advanceSibling(c);
        //if the char is found in a sibling
        if (success) {
          //return true
          return true;
        } 
        //if the char is not found in a sibling
        else {
          //return false
          return false;
        }
      }
    }

  /**
   * removes the last character from the current prefix in O(1) time. This 
   * method doesn't modify the dictionary.
   * @throws IllegalStateException if the current prefix is the empty string
   */
    public void retreat(){
      //if currentPrefix is empty, you cannot retreat, therefore throw exception
      if (currentPrefix.toString().length() == 0) {
        throw new IllegalStateException("calls retreat when current prefix is empty");
      } 
      //delete last letter in currentPrefix
      currentPrefix.deleteCharAt(currentPrefix.length()-1);
      //if currentPrefix is now empty
      if (currentPrefix.toString().length() == 0) {
        //if currentPrefix is empty, reset currentNode
        currentNode = null;
        return;
      }
      //if currentPrefix and currentNode is in sync
      if (numSeperateOut == 0 ) {
        //if currentNode has a valid parent link
        if (currentNode.parent != null) {
          //retreat currentNode
          currentNode = currentNode.parent;
        } 
        //if currentNode does not have a valid parent link
        else {
          //recursively retreat to previous sibling until a node is found with a valid parent link
          retreatSibling();
          //retreat currentNode
          currentNode = currentNode.parent;
        }
      }
      //if currentNode and currentPrefix were out of sync
      if (numSeperateOut > 0) {
        //remove 1 from out-of-sync tracker since we retreated a letter
        numSeperateOut--;
        //if currentNode and currentPrefix is no longer out of sync after the removal of the letter
        if (numSeperateOut == 0) {
          //set newWord to false
          newWord = false;
        }
      }
    }

    private void retreatSibling() {
      //retreat currentNode to the previous sibling
      currentNode = currentNode.previousSibling;
      //if the sibling has a valid parent, return
      if (currentNode.parent != null) {
        return;
      }
      //else, keep retreating to the previous sibling through recursion
      else {
        retreatSibling();
      }
    }

    private void retreatBackNode() {
      //retreat currentNode to its parentNode
      currentNode = currentNode.parent;
    }

  /**
   * resets the current prefix to the empty string in O(1) time
   */
    public void reset(){
      //resets currentPrefix
      currentPrefix = null;
      //resets currentNode
      currentNode = null;
    }
    
  /**
   * @return true if the current prefix is a word in the dictionary and false
   * otherwise. The running time is O(1).
   */
    public boolean isWord(){
      //if currentNode and currentPrefix are out of sync, it means it is not currently a word in the dictionary
      if (newWord == true) {
        //since currentNode and currentPrefix will be reset after isWord() is called, reset newWord as well
        newWord = false;
        //newWord is true, therefore it is not a word, return false
        return false;
      }
      //since currentNode and currentPrefix will be reset after isWord() is called, reset newWord as well
      newWord = false;
      //newWord is false, therefore it is a word, return true
      return true;
    }

  /**
   * adds the current prefix as a word to the dictionary (if not already a word)
   * The running time is O(alphabet size*length of the current prefix). 
   */
    public void add(){
      //call previous add method to add the currentPrefix to the dictionary
      add(currentPrefix.toString());
      //set newCurrentNode to last node added
      DLBNode newCurrentNode = getNode(currentNode, currentPrefix.toString(), 0);
      //set currentNode to newCurrentNode
      currentNode = newCurrentNode;     
      //since the new word is added, it will be in the dictionary, reset newWord
      newWord = false;
    }

  /** 
   * @return the number of words in the dictionary that start with the current 
   * prefix (including the current prefix if it is a word). The running time is 
   * O(1).
   */
    public int getNumberOfPredictions(){
      //if currentPrefix and currentNode is currently out of sync, it means the word is not in the dictionary
      if (newWord == true) {
        //therefore there will be no predictions, return 0
        return 0;
      }
      //else, the word is in the dictionary, and the number of words in the dictionary that starts with the currentPrefix is the size of the currentNode
      return currentNode.size;
    }
 
  /**
   * retrieves one word prediction for the current prefix. The running time is 
   * O(prediction.length())
   * @return a String or null if no predictions exist for the current prefix
   */
    public String retrievePrediction(){
      boolean success;
      String prediction;
      //make node pre to simulate currentNode
      pre = currentNode;
      //if currentPrefix and currentNode is currently out of sync, it means the word is not in the dictionary
      if (newWord) {
        //if it is not in the dictionary, there will not be a prediction
        return null;
      }
      //if currentNode is a word
      if (pre.isWord) {
        //return the currentPrefix that has been building along with currentNode
        return currentPrefix.toString();
      } 
      //if currentNode is not a word
      else {
        //recursively move down to the children of currentNode until it is a word
        success = moveDownForPrediction();
        //if a word is found from the children
        if (success) {
          //set the prediction to be the predictedPrefix from the recursive moveDownForPrediction() method
          prediction = prefixPrediction.toString();
          //reset predixPrediction
          prefixPrediction = null;
          //return the prediction
          return prediction;
        } 
        //if a prediction was not found
        else {
          //return null
          return null;
        }
      }
    }

    private boolean moveDownForPrediction() {
      //make new StringBuilder prefixPrediction to store predicted string to not change currentPrefix
      if (prefixPrediction == null) {
        //initialize prefixPrediction
        prefixPrediction = new StringBuilder();
        //add the letters we have so far from currentPrefix to the prefixPrediction
        prefixPrediction.append(currentPrefix);
      }
      //if the child of the node we are at is not null
      if (pre.child != null) {
        //set it to its child
        pre = pre.child;
        //add the letter of the child to the prefixPrediction
        prefixPrediction.append(pre.data);
        //if it is now a word
        if (pre.isWord) {
          //return true
          return true;
        } 
        //if it is still not a word
        else {
          boolean success;
          //continue to traverse down the children recursively
          success = moveDownForPrediction();
          //if a word is found in the children
          if (success) {
            //return true
            return true;
          } 
          //if a word is not found in the children
          else {
            //return false
            return false;
          }
        }
      } 
      //if the child of the node we are at is null
      else {
        //if the node we are at is a word
        if (pre.isWord) {
          //return true
          return true;
        } 
        //if the node we are at is not a word
        else {
          //return false
          return false;
        }
      }
    }

  /* ==============================
   * Helper methods for debugging.
   * ==============================
   */

  //print the subtrie rooted at the node at the end of the start String
  public void printTrie(String start){
    System.out.println("==================== START: DLB Trie Starting from \""+ start + "\" ====================");
    if(start.equals("")){
      printTrie(root, 0);
    } else {
      DLBNode startNode = getNode(root, start, 0);
      if(startNode != null){
        printTrie(startNode.child, 0);
      }
    }
    
    System.out.println("==================== END: DLB Trie Starting from \""+ start + "\" ====================");
  }

  //a helper method for printTrie
  private void printTrie(DLBNode node, int depth){
    if(node != null){
      for(int i=0; i<depth; i++){
        System.out.print(" ");
      }
      System.out.print(node.data);
      if(node.isWord){
        System.out.print(" *");
      }
      System.out.println(" (" + node.size + ")");
      printTrie(node.child, depth+1);
      printTrie(node.nextSibling, depth);
    }
  }

  //return a pointer to the node at the end of the start String 
  //in O(start.length() - index)
  private DLBNode getNode(DLBNode node, String start, int index){ //index is the position in the string we're starting the search from (current character in the search string). Starts at 0, incremented as we go
    if(start.length() == 0){
      return node;
    }
    DLBNode result = node;
    if(node != null){
      if((index < start.length()-1) && (node.data == start.charAt(index))) {
          result = getNode(node.child, start, index+1);
      } else if((index == start.length()-1) && (node.data == start.charAt(index))) {
          result = node;
      } else {
          result = getNode(node.nextSibling, start, index);
      }
    }
    return result;
  } 

  //The DLB node class
  private class DLBNode{
    private char data;
    private int size;
    private boolean isWord;
    private DLBNode nextSibling;
    private DLBNode previousSibling;
    private DLBNode child;
    private DLBNode parent;

    private DLBNode(char data){
        this.data = data;
        size = 0;
        isWord = false;
        nextSibling = previousSibling = child = parent = null;
    }
  }
}

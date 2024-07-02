### Analysis of the `TreeComponentGenerator` Class

#### General Structure

1. **Field `root`**: The root element of the family tree, initialized in the constructor.
2. **Maps `rootParents` and `rootOffspring`**: These map UUIDs to `FamilyRepeated` objects for ancestors and descendants, respectively.
3. **Methods `buildDescendantsTreeComponent` and `buildAncestorsTreeComponent`**: These methods are responsible for building the descendants and ancestors trees.
4. **Methods `buildDescendantsTree` and `buildAncestorsTree`**: They recursively gather data about descendants and ancestors.

#### Constructor

The constructor initializes the root element and the maps for ancestors and descendants. After that, it calls methods to build the descendants and ancestors trees.

#### Method `buildFamilyTreeComponent`

This method is the main one and is called to build the complete family tree. It:
- Builds the header for the family tree.
- Calls methods to build the descendants and ancestors components, adding them to the overall tree.

### Method `buildDescendantsTreeComponent`

#### Reverse Recursion

The method uses reverse recursion (recursion that goes from deeper levels to higher levels) to build the descendants tree:
1. **Recursive processing of descendants**: First, the method processes all the children of the current family member, creating reverse recursion.
2. **Adding the current element**: After processing all the children, the method adds the current family member to the tree. This ensures that descendants appear in the correct order, where lower levels are displayed before higher levels.

#### Example of a Descendants Tree

Assume the player has a son, a grandson, and a great-grandson. The descendants tree will look like this:

```
    ┌─ (♂) Great-Grandson
  ┌─ (♂) Grandson
┌─ (♂) Son
```

Here, the deepest levels (great-grandson) are processed first, then their parents (grandson), and finally, the son is added.

### Method `buildAncestorsTreeComponent`

#### Forward Recursion

The method uses forward recursion to build the ancestors tree:
1. **Recursive processing of ancestors**: First, the method processes all the parents of the current family member, starting with the closest ancestors and moving deeper.
2. **Adding the current element**: After processing all the parents, the method adds the current family member to the tree. This ensures that ancestors appear in the correct order, where higher levels are displayed before lower levels.

#### Example of an Ancestors Tree

Assume the player has a father, a grandfather, and a great-grandfather. The ancestors tree will look like this:

```
  └─ (♂) Great-Grandfather
    └─ (♂) Grandfather
      └─ (♂) Father
```

Here, the most distant ancestors (great-grandfather) are processed first, then their children (grandfather), and finally, the father is added.

### Complete Family Tree

The `buildFamilyTreeComponent` method combines both trees (descendants and ancestors) along with the root element (the player) to create a complete family tree.

#### Example of a Complete Family Tree

Assume the player has a son, a grandson, a great-grandson, a father, a grandfather, and a great-grandfather. The complete tree will look like this:

```
    ┌─ (♂) Great-Grandson
  ┌─ (♂) Grandson
┌─ (♂) Son
 Descendants
Family Tree (♂) Player
 Ancestors
  └─ (♂) Father
    └─ (♂) Grandfather
      └─ (♂) Great-Grandfather
```

Here:
- First, the descendants tree is built using reverse recursion.
- Then, the central element (the player) is added.
- Finally, the ancestors tree is built using forward recursion.

### Conclusion

The `TreeComponentGenerator` class uses recursive methods to build the descendants and ancestors trees. The `buildDescendantsTreeComponent` method uses reverse recursion to correctly display descendants, while the `buildAncestorsTreeComponent` method uses forward recursion to correctly display ancestors. This allows the creation of a complete family tree that reflects all relationships in the correct order.

---

### Class TreeStringGenerator

The `TreeStringGenerator` class works on the same principle, with the difference that it builds a string instead of a component, using `StringBuilder`.
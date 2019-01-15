import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by tianze
 * 2019/1/15 16:13
 */
public class Learn1 {
    public static class TreeNode {
        int val = 0;
        TreeNode left = null;
        TreeNode right = null;

        public TreeNode(int val) {
            this.val = val;

        }
    }

    //前序遍历递归实现
    public static void  preOrderTraverseRecursive(TreeNode root){
        if(root == null){
            return;
        }

        System.out.print(root.val + " ");
        preOrderTraverseRecursive(root.left);
        preOrderTraverseRecursive(root.right);
    }

    //前序遍历非递归实现
    public static void preOrderTraverse(TreeNode root){
        if(root == null){
            return;
        }

        Stack<TreeNode> linkedList = new Stack<>();

        while (root != null || !linkedList.isEmpty()){
            if(root != null){
                System.out.print(root.val + " ");
                linkedList.push(root);
                root = root.left;
            }
            else {
                TreeNode node = linkedList.pop();
                root = node.right;
            }
        }

        System.out.println();
    }

    //中序遍历递归实现
    public static void  inOrderTraverseRecursive(TreeNode root){
        if(root == null){
            return;
        }

        inOrderTraverseRecursive(root.left);
        System.out.print(root.val + " ");
        inOrderTraverseRecursive(root.right);
    }

    //中序遍历非递归实现
    public static void inOrderTraverse(TreeNode root){
        if(root == null){
            return;
        }

        Stack<TreeNode> linkedList = new Stack<>();

        while (root != null || !linkedList.isEmpty()){
            if(root != null){
                linkedList.push(root);
                root = root.left;
            }
            else {
                TreeNode node = linkedList.pop();
                System.out.print(node.val + " ");
                root = node.right;
            }
        }

        System.out.println();
    }

    //后序遍历递归实现
    public static void  postOrderTraverseRecursive(TreeNode root){
        if(root == null){
            return;
        }

        postOrderTraverseRecursive(root.left);
        postOrderTraverseRecursive(root.right);
        System.out.print(root.val + " ");
    }

    //后序遍历非递归实现一
    public static void postOrderTraverse1(TreeNode root){
        if(root == null){
            return;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        TreeNode temp = null;
        TreeNode flag = root;

        while (!stack.empty()){
            temp = stack.peek();

            if(temp.left != null && flag != temp.left && flag != temp.right){
                stack.push(temp.left);
            }
            else if(temp.right != null && flag != temp.right){
                stack.push(temp.right);
            }
            else {
                System.out.print(stack.pop().val + " ");
                flag = temp;
            }
        }

        System.out.println();
    }

    //后序遍历非递归实现二
    public static void postOrderTraverse2(TreeNode root){
        if(root == null){
            return;
        }

        Stack<TreeNode> stack = new Stack<>();
        Stack<TreeNode> result = new Stack<>();
        stack.push(root);

        while (!stack.empty()){
            TreeNode temp = stack.pop();
            result.push(temp);

            if(temp.left != null){
                stack.push(temp.left);
            }

            if(temp.right != null){
                stack.push(temp.right);
            }
        }

        while (!result.empty()){
            System.out.print(result.pop().val + " ");
        }
        System.out.println();
    }

    //层次遍历实现
    public static void levelTraverse(TreeNode root){
        if(root == null){
            return;
        }

        LinkedList<TreeNode> linkedList = new LinkedList<>();
        linkedList.add(root);

        while (linkedList.size() != 0){
            TreeNode temp = linkedList.poll();
            System.out.print(temp.val + " ");

            if(temp.left != null){
                linkedList.add(temp.left);
            }
            if(temp.right != null){
                linkedList.add(temp.right);
            }
        }

        System.out.println();
    }



    public static void main(String[] args){
        TreeNode node1 = new TreeNode(1);
        TreeNode node2 = new TreeNode(2);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);
        TreeNode node5 = new TreeNode(5);
        TreeNode node6 = new TreeNode(6);
        TreeNode node7 = new TreeNode(7);

        node1.left = node2;
        node1.right = node3;
        node2.left = node4;
        node2.right = node5;
        node3.left = node6;
        node3.right = node7;

        preOrderTraverseRecursive(node1);
        System.out.println();
        preOrderTraverse(node1);

        inOrderTraverseRecursive(node1);
        System.out.println();
        inOrderTraverse(node1);

        postOrderTraverseRecursive(node1);
        System.out.println();
        postOrderTraverse1(node1);
        postOrderTraverse2(node1);

        levelTraverse(node1);
    }

}

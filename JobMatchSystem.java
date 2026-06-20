import java.util.*;

// =========================================================================
// MODEL CLASSES
// =========================================================================

class Candidate {
    int id;
    String name;
    int experienceMonths; 
    int matchScore; // Score from 1 to 100 for ranking applications

    public Candidate(int id, String name, int experienceMonths, int matchScore) {
        this.id = id;
        this.name = name;
        this.experienceMonths = experienceMonths;
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        return "Candidate[ID=" + id + ", Name='" + name + "', Experience=" + experienceMonths + " months, MatchScore=" + matchScore + "]";
    }
}

class JobApplication {
    String jobTitle;
    int timeRequired; // Application/Prep cost factor
    int expectedSalary; // Value factor to maximize

    public JobApplication(String jobTitle, int timeRequired, int expectedSalary) {
        this.jobTitle = jobTitle;
        this.timeRequired = timeRequired;
        this.expectedSalary = expectedSalary;
    }
}

// =========================================================================
// MODULE 1 (M1 - CO1): TREES & BALANCED SEARCH STRUCTURES (AVL Tree)
// Feature: Balanced candidate records database indexed by CandidateID
// =========================================================================

class AVLNode {
    Candidate candidate;
    int height;
    AVLNode left, right;

    AVLNode(Candidate candidate) {
        this.candidate = candidate;
        this.height = 1;
    }
}

class CandidateAVLTree {
    private AVLNode root;

    private int height(AVLNode node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    public void insert(Candidate candidate) {
        root = insertRec(root, candidate);
    }

    private AVLNode insertRec(AVLNode node, Candidate candidate) {
        if (node == null) return new AVLNode(candidate);

        if (candidate.id < node.candidate.id)
            node.left = insertRec(node.left, candidate);
        else if (candidate.id > node.candidate.id)
            node.right = insertRec(node.right, candidate);
        else 
            return node; // Duplicate IDs ignored

        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = getBalance(node);

        if (balance > 1 && candidate.id < node.left.candidate.id) return rightRotate(node);
        if (balance < -1 && candidate.id > node.right.candidate.id) return leftRotate(node);
        if (balance > 1 && candidate.id > node.left.candidate.id) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (balance < -1 && candidate.id < node.right.candidate.id) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;
    }

    public Candidate search(int candidateId) {
        return searchRec(root, candidateId);
    }

    private Candidate searchRec(AVLNode node, int id) {
        if (node == null) return null;
        if (node.candidate.id == id) return node.candidate;
        return id < node.candidate.id ? searchRec(node.left, id) : searchRec(node.right, id);
    }

    public void inorderTraversal() {
        inorderRec(root);
    }

    private void inorderRec(AVLNode node) {
        if (node != null) {
            inorderRec(node.left);
            System.out.println("  " + node.candidate);
            inorderRec(node.right);
        }
    }
}

// =========================================================================
// MODULE 2 (M2 - CO2): MULTIWAY TREES & RANGE QUERY STRUCTURES (Segment Tree)
// Feature: Analytics tracking application metrics over specified time ranges
// =========================================================================

class ApplicationSegmentTree {
    int[] tree;
    int n;

    public ApplicationSegmentTree(int[] dailyApplications) {
        this.n = dailyApplications.length;
        this.tree = new int[4 * n];
        build(dailyApplications, 0, 0, n - 1);
    }

    private void build(int[] arr, int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
            return;
        }
        int mid = start + (end - start) / 2;
        build(arr, 2 * node + 1, start, mid);
        build(arr, 2 * node + 2, mid + 1, end);
        tree[node] = tree[2 * node + 1] + tree[2 * node + 2];
    }

    public int queryRange(int L, int R) {
        if (L < 0 || R >= n || L > R) return 0;
        return queryRec(0, 0, n - 1, L, R);
    }

    private int queryRec(int node, int start, int end, int L, int R) {
        if (R < start || end < L) return 0; 
        if (L <= start && end <= R) return tree[node]; 

        int mid = start + (end - start) / 2;
        int leftSum = queryRec(2 * node + 1, start, mid, L, R);
        int rightSum = queryRec(2 * node + 2, mid + 1, end, L, R);
        return leftSum + rightSum;
    }
}

// =========================================================================
// MODULE 3 (M3 - CO3): GRAPH ALGORITHMS FOR JOB NETWORKS
// Feature: Skills & job relationship tracking, path connectivity, dependency loops
// =========================================================================

class SkillJobNetwork {
    private Map<String, List<String>> adjList = new HashMap<>();

    public void addSkillOrJob(String element) {
        adjList.putIfAbsent(element, new ArrayList<>());
    }

    public void addDependency(String from, String to) {
        addSkillOrJob(from);
        addSkillOrJob(to);
        adjList.get(from).add(to);
    }

    public boolean isConnected(String start, String end) {
        if (!adjList.containsKey(start) || !adjList.containsKey(end)) return false;
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (curr.equals(end)) return true;
            for (String neighbor : adjList.getOrDefault(curr, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    public boolean detectDependencyLoop() {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (String node : adjList.keySet()) {
            if (dfsLoopCheck(node, visited, recStack)) return true;
        }
        return false;
    }

    private boolean dfsLoopCheck(String node, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        recStack.add(node);

        for (String neighbor : adjList.getOrDefault(node, new ArrayList<>())) {
            if (dfsLoopCheck(neighbor, visited, recStack)) return true;
        }
        recStack.remove(node);
        return false;
    }
}

// =========================================================================
// MODULE 4 (M4 - CO4): SHORTEST PATH OPTIMIZATION (Dijkstra's)
// Feature: Finding the shortest/least-cost skill-acquisition path for a role
// =========================================================================

class CareerPathRouter {
    static class Edge {
        int targetSkillId, financialCost;
        Edge(int targetSkillId, int financialCost) {
            this.targetSkillId = targetSkillId;
            this.financialCost = financialCost;
        }
    }

    public static int findCheapestPath(Map<Integer, List<Edge>> graph, int sourceSkill, int targetJobSkill) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> totalCosts = new HashMap<>();
        
        pq.add(new int[]{sourceSkill, 0});
        totalCosts.put(sourceSkill, 0);

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int currentCost = current[1];

            if (u == targetJobSkill) return currentCost;
            if (currentCost > totalCosts.getOrDefault(u, Integer.MAX_VALUE)) continue;

            for (Edge edge : graph.getOrDefault(u, new ArrayList<>())) {
                int v = edge.targetSkillId;
                int newCost = currentCost + edge.financialCost;
                if (newCost < totalCosts.getOrDefault(v, Integer.MAX_VALUE)) {
                    totalCosts.put(v, newCost);
                    pq.add(new int[]{v, newCost});
                }
            }
        }
        return -1; 
    }
}

// =========================================================================
// MODULE 5 (M5 - CO5): ADVANCED SORTING & DATA RANKING (Max-Heap Queue)
// Feature: Priority queue implementation to identify and rank top applicants
// =========================================================================

class TalentTriageSystem {
    // Dynamic Max-Heap prioritizing candidates with the highest structural match score
    private PriorityQueue<Candidate> applicantPool = new PriorityQueue<>((c1, c2) -> c2.matchScore - c1.matchScore);

    public void addApplicant(Candidate c) {
        applicantPool.add(c);
    }

    public Candidate extractTopCandidate() {
        return applicantPool.poll();
    }

    public boolean isEmpty() {
        return applicantPool.isEmpty();
    }
}

// =========================================================================
// MODULE 6 (M6 - CO6): GREEDY ALGORITHMS & DYNAMIC PROGRAMMING (0/1 Knapsack)
// Feature: Optimizing job seeker portfolio applications to maximize salary return
// =========================================================================

class JobPortfolioOptimizer {
    public static int maximizePortfolioSalary(List<JobApplication> options, int operationalTimeLimit) {
        int n = options.size();
        int[][] dp = new int[n + 1][operationalTimeLimit + 1];

        for (int i = 1; i <= n; i++) {
            JobApplication app = options.get(i - 1);
            for (int w = 1; w <= operationalTimeLimit; w++) {
                if (app.timeRequired <= w) {
                    dp[i][w] = Math.max(app.expectedSalary + dp[i - 1][w - app.timeRequired], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        return dp[n][operationalTimeLimit]; 
    }
}

// =========================================================================
// JOBMATCH INTERACTIVE MAIN SYSTEM CONTROLLER
// =========================================================================

public class JobMatchSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Persistent Systems
        CandidateAVLTree avlTree = new CandidateAVLTree();
        SkillJobNetwork network = new SkillJobNetwork();
        TalentTriageSystem triageSystem = new TalentTriageSystem();

        System.out.println("==================================================================");
        System.out.println("     WELCOME TO THE JOBMATCH PLATFORM INTERACTIVE CONTROLLER      ");
        System.out.println("==================================================================");

        while (true) {
            System.out.println("\n--- MAIN CONTROL PANEL ---");
            System.out.println("1. [M1-CO1] Manage Candidate Profiles Directory (AVL Tree)");
            System.out.println("2. [M2-CO2] Run Platform Metrics Tracking Analytics (Segment Tree)");
            System.out.println("3. [M3-CO3] Verify Skill Requirements & Dependency Loops (Graphs)");
            System.out.println("4. [M4-CO4] Optimize Candidate Skill Acquisition Paths (Dijkstra)");
            System.out.println("5. [M5-CO5] Run Automated Talent Recruitment Screening Desk (Max-Heap)");
            System.out.println("6. [M6-CO6] Optimize Applicant Job Portfolio Values (0/1 DP Knapsack)");
            System.out.println("7. Shut Down System Application");
            System.out.print("Select a choice (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    System.out.println("\n--- [M1-CO1] AVL CANDIDATE DIRECTORY MANAGER ---");
                    System.out.println("1. Register Candidate Profile");
                    System.out.println("2. Locate Candidate profile by CandidateID");
                    System.out.println("3. Run Complete Directory Structural Print (Inorder Traversal)");
                    System.out.print("Choose action: ");
                    int avlChoice = scanner.nextInt();
                    if (avlChoice == 1) {
                        System.out.print("Enter Candidate ID (Integer): ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Candidate Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Professional Experience (In Months): ");
                        int exp = scanner.nextInt();
                        System.out.print("Enter System Initial Match Metric Score (1-100): ");
                        int score = scanner.nextInt();
                        avlTree.insert(new Candidate(id, name, exp, score));
                        System.out.println("Candidate registered and indexed via tree balance parameters successfully.");
                    } else if (avlChoice == 2) {
                        System.out.print("Enter Candidate ID to look up: ");
                        int searchId = scanner.nextInt();
                        Candidate c = avlTree.search(searchId);
                        if (c != null) System.out.println("Record Found -> " + c);
                        else System.out.println("Error: Target CandidateID entry does not match record indexes.");
                    } else if (avlChoice == 3) {
                        System.out.println("Auditing Inorder Sequence Index Register Layout:");
                        avlTree.inorderTraversal();
                    }
                    break;

                case 2:
                    System.out.println("\n--- [M2-CO2] APPLICATION VOLUME TIME MONITOR ---");
                    System.out.print("Enter number of tracking calendar period units: ");
                    int totalPeriods = scanner.nextInt();
                    int[] rawTimeline = new int[totalPeriods];
                    for (int i = 0; i < totalPeriods; i++) {
                        System.out.print("  Enter total applications submitted in Period [" + i + "]: ");
                        rawTimeline[i] = scanner.nextInt();
                    }
                    ApplicationSegmentTree segTree = new ApplicationSegmentTree(rawTimeline);
                    System.out.println("Segment Tree compiled seamlessly.");
                    System.out.print("Enter Start Period Window Bound (L): ");
                    int L = scanner.nextInt();
                    System.out.print("Enter End Period Window Bound (R): ");
                    int R = scanner.nextInt();
                    System.out.println("Aggregated total submission actions completed: " + segTree.queryRange(L, R) + " applications.");
                    break;

                case 3:
                    System.out.println("\n--- [M3-CO3] STRUCTURAL SKILL RELATIONSHIP NETWORKS ---");
                    System.out.println("1. Create New Prerequisite Skill Mapping Connection");
                    System.out.println("2. Validate Career Roadmap Path Route Availability (BFS)");
                    System.out.println("3. Run Cyclic Prerequisite Loop Audit Assessment (DFS)");
                    System.out.print("Select setting choice: ");
                    int graphChoice = scanner.nextInt();
                    scanner.nextLine();
                    if (graphChoice == 1) {
                        System.out.print("Enter Prerequisite Skill Name (e.g., Core_Java): ");
                        String from = scanner.nextLine();
                        System.out.print("Enter Target Dependent Skill/Job (e.g., Spring_Boot): ");
                        String to = scanner.nextLine();
                        network.addDependency(from, to);
                        System.out.println("Dependency relation recorded successfully.");
                    } else if (graphChoice == 2) {
                        System.out.print("Enter Starting Acquired Skill: ");
                        String from = scanner.nextLine();
                        System.out.print("Enter Target Role / Skill Node Goal: ");
                        String to = scanner.nextLine();
                        System.out.println("Roadmap Path Option Availability: " + network.isConnected(from, to));
                    } else if (graphChoice == 3) {
                        System.out.println("Auditing skills mapping registers for locking redundancy loops...");
                        System.out.println("Infinite Catch-22 Skill Loop Detected: " + network.detectDependencyLoop());
                    }
                    break;

                case 4:
                    System.out.println("\n--- [M4-CO4] CRITICAL CAREER ACQUISITION ROUTER ---");
                    System.out.print("Enter total number of catalog skill nodes: ");
                    int nodes = scanner.nextInt();
                    Map<Integer, List<CareerPathRouter.Edge>> skillMap = new HashMap<>();
                    for (int i = 0; i < nodes; i++) skillMap.put(i, new ArrayList<>());

                    System.out.print("Enter count total of study path learning tracks: ");
                    int lanes = scanner.nextInt();
                    for (int i = 0; i < lanes; i++) {
                        System.out.println("Course Path Configuration Setup [" + i + "]:");
                        System.out.print("  Source Skill ID (0 to " + (nodes - 1) + "): ");
                        int u = scanner.nextInt();
                        System.out.print("  Target Destination Skill ID: ");
                        int v = scanner.nextInt();
                        System.out.print("  Tuition Learning Cost (in Dollars/Hours): ");
                        int cost = scanner.nextInt();
                        skillMap.get(u).add(new CareerPathRouter.Edge(v, cost));
                    }
                    System.out.print("Enter Current Skill Node ID possessed by candidate: ");
                    int src = scanner.nextInt();
                    System.out.print("Enter Targeted Qualified Job Skill Node ID: ");
                    int dest = scanner.nextInt();
                    int minCost = CareerPathRouter.findCheapestPath(skillMap, src, dest);
                    if (minCost != -1) System.out.println("Dijkstra tracking optimization finished! Minimum investment requirement cost: $" + minCost);
                    else System.out.println("Routing Alert: Target skill classification is unreachable via existing study paths.");
                    break;

                case 5:
                    System.out.println("\n--- [M5-CO5] TALENT SCREENING PRIORITY DISPATCH HUB ---");
                    System.out.println("1. Add Incoming Candidate Application to Screening Pool");
                    System.out.println("2. Pull / Review Next Top Ranked Applicant Profile");
                    System.out.print("Selection: ");
                    int triChoice = scanner.nextInt();
                    scanner.nextLine();
                    if (triChoice == 1) {
                        System.out.print("Enter Candidate ID Number: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Candidate Full Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Qualification Metric/Match Score (1-100): ");
                        int score = scanner.nextInt();
                        triageSystem.addApplicant(new Candidate(id, name, 0, score));
                        System.out.println("Application placed successfully in global Max-Heap sorting index.");
                    } else if (triChoice == 2) {
                        if (!triageSystem.isEmpty()) {
                            System.out.println("Retrieved Top-Ranked Applicant profile: " + triageSystem.extractTopCandidate());
                        } else {
                            System.out.println("Screening pool empty. No new candidate application matching profiles pending review.");
                        }
                    }
                    break;

                case 6:
                    System.out.println("\n--- [M6-CO6] APPLICANT SEEKER PORTFOLIO OPTIMIZER ---");
                    System.out.print("Enter total number of concurrent open job opportunities under review: ");
                    int itemCount = scanner.nextInt();
                    List<JobApplication> options = new ArrayList<>();
                    for (int i = 0; i < itemCount; i++) {
                        scanner.nextLine();
                        System.out.print("  Enter Job Target Post Title [" + i + "]: ");
                        String name = scanner.nextLine();
                        System.out.print("  Enter custom interview prep / portfolio setup time cost factor: ");
                        int cost = scanner.nextInt();
                        System.out.print("  Enter listed expected annual compensation metric value ($k): ");
                        int value = scanner.nextInt();
                        options.add(new JobApplication(name, cost, value));
                    }
                    System.out.print("Enter applicant total available preparation time window capacity limit: ");
                    int cap = scanner.nextInt();
                    int solution = JobPortfolioOptimizer.maximizePortfolioSalary(options, cap);
                    System.out.println("0/1 Knapsack DP Matrix processing completed!");
                    System.out.println("Maximized baseline application value index yield achieved: $" + solution + "k");
                    break;

                case 7:
                    System.out.println("\nFinalizing platform metrics tracking logs. JobMatch system terminating safely. Goodbye!");
                    scanner.close();
                    System.exit(0);

                default:
                    System.out.println("Invalid setting entry option! Please select a valid numerical action from the control menu panel.");
            }
        }
    }
}
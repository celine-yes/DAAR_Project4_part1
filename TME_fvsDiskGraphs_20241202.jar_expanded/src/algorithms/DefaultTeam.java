package algorithms;

import java.awt.Point;
import java.util.*;

public class DefaultTeam {

    public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
        int initialPopulationSize = 5; // Taille de la population initiale
        ArrayList<ArrayList<Point>> solList = new ArrayList<>();

        // Génération de la population initiale
        for (int i = 0; i < initialPopulationSize; i++) {
            ArrayList<Point> solution = greedy(points, edgeThreshold);
            if (isValid(points, solution, edgeThreshold)) {
                solList.add(solution);
            }
        }

        assert solList.size() > 0 : "La liste initiale de solutions est vide";
        System.out.println("Nombre de solutions initiales : " + solList.size());

        // Vérification de la validité des solutions
        assert isValidList(solList, points, edgeThreshold) : "Certaines solutions initiales ne sont pas valides";

        // Appel de la méthode d'amélioration
        ArrayList<Point> bestSolution = improveSolutions(solList, points, edgeThreshold);
        System.out.println("Meilleure solution trouvée de taille : " + bestSolution.size());
        return bestSolution;
    }

    private boolean isValidList(ArrayList<ArrayList<Point>> solList, ArrayList<Point> points, int edgeThreshold) {
        for (ArrayList<Point> solution : solList) {
            if (!isValid(points, solution, edgeThreshold)) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Point> improveSolutions(ArrayList<ArrayList<Point>> solList, ArrayList<Point> points, int edgeThreshold) {
        int iterations = 2;

        for (int i = 0; i < iterations; i++) {
            ArrayList<ArrayList<Point>> nextSolList = new ArrayList<>();

            // Mutation : appliquer KLS sur chaque solution
            for (ArrayList<Point> solution : solList) {
                // Faire une copie de la solution avant de la modifier
                ArrayList<Point> solutionCopy = (ArrayList<Point>) solution.clone();
                ArrayList<Point> mutatedSolution = localSearch(solutionCopy, points, edgeThreshold);
                if (isValid(points, mutatedSolution, edgeThreshold)) {
                    nextSolList.add(mutatedSolution);
                } else {
                    nextSolList.add(solution); // Si la mutation n'est pas valide, garder la solution originale
                }
            }

            // Génération : ajouter une nouvelle solution via greedy()
            ArrayList<Point> newSolution = greedy(points, edgeThreshold);
            if (isValid(points, newSolution, edgeThreshold)) {
                nextSolList.add(newSolution);
            }

            // Sélection : trier les solutions et supprimer la pire
            Collections.sort(nextSolList, Comparator.comparingInt(ArrayList::size));
            if (nextSolList.size() > solList.size()) {
                nextSolList.remove(nextSolList.size() - 1); // Supprimer la pire solution
            }

            // Mise à jour de la liste de solutions
            solList = nextSolList;

            // Afficher le score de la meilleure solution après chaque itération
            System.out.println("Itération " + (i + 1) + " : Meilleure taille de solution = " + solList.get(0).size());
        }

        // Retourner la meilleure solution trouvée
        Collections.sort(solList, Comparator.comparingInt(ArrayList::size));
        return solList.get(0);
    }

    private ArrayList<Point> greedy(ArrayList<Point> pointsIn, int edgeThreshold) {
        ArrayList<Point> points = (ArrayList<Point>) pointsIn.clone();
        ArrayList<Point> result = new ArrayList<>();

        // Copie des points pour éviter les modifications inattendues
        ArrayList<Point> rest = removeDuplicates(points);

        Random rand = new Random();

        while (!isValid(pointsIn, result, edgeThreshold)) {
            // Calculer les degrés de tous les sommets restants
            Map<Point, Integer> degreeMap = new HashMap<>();
            for (Point p : rest) {
                int deg = degree(p, rest, edgeThreshold);
                degreeMap.put(p, deg);
            }

            // Trier les sommets par degré décroissant
            List<Point> sortedPoints = new ArrayList<>(rest);
            sortedPoints.sort((p1, p2) -> degreeMap.get(p2) - degreeMap.get(p1));

            // Sélectionner les 5 sommets ayant les degrés les plus élevés
            int topK = Math.min(5, sortedPoints.size());
            List<Point> topCandidates = sortedPoints.subList(0, topK);

            // Choisir un sommet aléatoirement parmi les top 5
            Point chosenOne = topCandidates.get(rand.nextInt(topCandidates.size()));

            result.add(chosenOne);
            rest.remove(chosenOne);
        }

        return result;
    }

    
    private ArrayList<Point> localSearch(ArrayList<Point> firstSolution, ArrayList<Point> points, int edgeThreshold) {
        ArrayList<Point> current = removeDuplicates(firstSolution);
        ArrayList<Point> next = (ArrayList<Point>)current.clone();

        System.out.println("LS. First sol: " + current.size());

        do {
          current = next;
          next = remove2add1(current, points,edgeThreshold);
          System.out.println("LS. Current sol: " + current.size() + ". Found next sol: "+next.size());
        } while (score(current)>score(next));
        
        System.out.println("LS. Last sol: " + current.size());
        return next;

    //  return current;
      }

    private ArrayList<Point> remove2add1(ArrayList<Point> candidate, ArrayList<Point> points, int edgeThreshold) {
        ArrayList<Point> test = removeDuplicates(candidate);
        ArrayList<Point> rest = removeDuplicates(points);
        rest.removeAll(test);

        for (int i = 0; i < test.size(); i++) {
            for (int j = i + 1; j < test.size(); j++) {
                Point p = test.get(i);
                Point q = test.get(j);

                // Faire une copie de 'test' pour éviter les modifications sur la liste originale
                ArrayList<Point> tempTest = (ArrayList<Point>) test.clone();
                tempTest.remove(q);
                tempTest.remove(p);

                for (Point r : rest) {
                    tempTest.add(r);
                    if (isValid(points, tempTest, edgeThreshold)) {
                        return tempTest;
                    }
                    tempTest.remove(r);
                }
            }
        }

        return candidate;
    }

    // Méthode isValid
    public boolean isValid(ArrayList<Point> origPoints, ArrayList<Point> fvs, int edgeThreshold){
        ArrayList<Point> vertices = new ArrayList<Point>();
        for (Point p:origPoints) if (!isMember(fvs,p)) vertices.add((Point)p.clone());

        // Recherche de cycles dans le sous-graphe induit par origPoints \ fvs
        while (!vertices.isEmpty()){
            ArrayList<Point> green = new ArrayList<Point>();
            green.add((Point)vertices.get(0).clone());
            ArrayList<Point> black = new ArrayList<Point>();

            while (!green.isEmpty()){
                for (Point p:neighbor(green.get(0),vertices,edgeThreshold)){
                    if (green.get(0).equals(p)) continue;
                    if (isMember(black,p)) return false;
                    if (isMember(green,p)) return false;
                    green.add((Point)p.clone());
                }
                black.add((Point)green.get(0).clone());
                vertices.remove(green.get(0));
                green.remove(0);
            }
        }

        return true;
    }

    private boolean isMember(ArrayList<Point> points, Point p) {
        return points.contains(p);
    }

    private ArrayList<Point> neighbor(Point p, ArrayList<Point> vertices, int edgeThreshold) {
        ArrayList<Point> result = new ArrayList<>();
        for (Point q : vertices) {
            if (!p.equals(q) && isEdge(p, q, edgeThreshold)) {
                result.add(q);
            }
        }
        return result;
    }

    private ArrayList<Point> removeDuplicates(ArrayList<Point> points) {
        Set<Point> set = new HashSet<>(points);
        return new ArrayList<>(set);
    }

    private boolean isEdge(Point p, Point q, int edgeThreshold) {
        return p.distance(q) < edgeThreshold;
    }

    private int degree(Point p, ArrayList<Point> points, int edgeThreshold) {
        int degree = 0;
        for (Point q : points) {
            if (!p.equals(q) && isEdge(p, q, edgeThreshold)) {
                degree++;
            }
        }
        return degree;
    }

    private int score(ArrayList<Point> candidate) {
        return candidate.size();
    }
}

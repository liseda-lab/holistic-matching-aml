package liseda.matcha.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Anchoring {

	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
		
		// times and decimals
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss yyyy/MM/dd"); 
		LocalDateTime start = LocalDateTime.now();
		DecimalFormat df = new DecimalFormat("###.###");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs);

		// define arguments
		String o1 = args[0];
		String o2 = args[1];
		String path = args[2];

		System.out.println(" ");
		System.out.println("Anchoring " + o1 + " and " + o2 + " at " + dtf.format(start));

		// create new file
		FileWriter fw = new FileWriter(path + "/anchoring.tsv", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);

		// get ontology names and add them to doc
		String oo1 = StringUtils.substringBefore(o1, ".");
		String oo2 = StringUtils.substringBefore(o2, ".");
		out.print("\n" + "Matcha_" + o1 + "/" + o2 + "\t");
		out.flush();

		// define file paths
		String sourcePath = path + "/ontologies/" + o1; // path to o1 file
		String targetPath = path + "/ontologies/" + o2; // path to o2 file

		// open ontologies
		long load = System.currentTimeMillis()/1000; // times
		Settings set = Settings.getInstance();
		StopList.init(ResourceManager.getStopSet());
		Ontology source = OntologyReader.parseInputOntology(sourcePath);
		Ontology target = OntologyReader.parseInputOntology(targetPath);
		set.defaultConfig(source,target);
		load = System.currentTimeMillis()/1000 - load; // times
		
		int sc = source.count(EntityType.CLASS);
		int tc = target.count(EntityType.CLASS);
		Ontology smallest = source;
		Ontology largest = target;
		if(sc <= tc) {
			smallest =  source;
			largest = target;
		}

		if(tc < sc) {
			smallest = target;
			largest = source;
		}

		long match = System.currentTimeMillis()/1000; // times
		// DirectXRefMatcher
		DirectXRefMatcherRefs refMatcher = new DirectXRefMatcherRefs();
		Alignment refs = refMatcher.match(smallest, largest, EntityType.CLASS, 0.6);
		System.out.println("DirectXRefMatcher mappings: " + refs.size());

		// Lexical Matcher
		LexicalMatcher lm = new LexicalMatcher();
		Alignment lex = lm.match(smallest, largest, EntityType.CLASS, 0.6);
		System.out.println("Lexical Matcher mappings: " + lex.size());

		match = System.currentTimeMillis()/1000 - match; // times

		// save alignment and print to doc
		refs.addAll(lex);
		System.out.println("Final alignment: " + refs.size());
		String outPath = path + "/ontologies/" + oo1 + "_" + oo2 + ".owl";
		out.print(load + "\t" + match + "\t" + refs.size());
		out.close();
		AlignmentIOOWL.save(refs, outPath);

	}

}

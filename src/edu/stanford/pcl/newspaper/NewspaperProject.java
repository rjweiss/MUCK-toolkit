package edu.stanford.pcl.newspaper;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 4/28/12
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */

public class NewspaperProject {
    public static void main(String[] args) throws Exception {

//        Mongo m = new Mongo();
//        DB db = m.getDB("test");
//        DBCollection articles = db.getCollection("articles");
//        DBObject myDoc = articles.findOne();
//        DBCursor cur;
//        BasicDBObject query = new BasicDBObject();

//        Calendar cal = new GregorianCalendar();
//        cal.set(2010, 0, 1); // retrieving from January 1st, 2010
//        Date fromDate = cal.getTime();
//        query.put("date", new BasicDBObject("$gte", fromDate));
//        System.out.println(articles.count(query)); //returns 157134 total articles

        String text = "Supporters of Bailout Claim Victory in Greek Election\n" +
                "By RACHEL DONADIO\n" +
                "ATHENS — Greek voters on Sunday gave a narrow victory in parliamentary elections to a party that had supported a bailout for the country’s failed economy. The vote was widely seen as a last chance for Greece to remain in the euro zone, and the results had an early rallying effect on world markets.\n" +
                "\n" +
                "Greece’s choice was welcomed by the finance ministers of the euro zone countries, who in a statement on Sunday night in Brussels said the outcome of the vote “should allow for the formation of a government that will carry the support of the electorate to bring Greece back on a path of sustainable growth.”\n" +
                "\n" +
                "While the election afforded Greece a brief respite from a rapid downward spiral, it is not likely to prevent a showdown between the next government and the country’s so-called troika of foreign creditors — the European Commission, the European Central Bank and the International Monetary Fund — over the terms of a bailout agreement.\n" +
                "\n" +
                "Even the most pro-Europe of Greece’s political parties, the conservative New Democracy, which came in first, has said a less austere agreement is crucial to a country with a 22 percent unemployment rate and the rising prospect of social unrest.\n" +
                "\n" +
                "The euro zone ministers pledged to help Greece transform its economy and said continued fiscal and structural changes were the best way to cope with its economic challenges. “The Eurogroup reiterates its commitment to assist Greece in its adjustment effort in order to address the many challenges the economy is facing,” the statement said.\n" +
                "\n" +
                "The ministers added that representatives of Greece’s creditors would discuss emergency loans and changes as soon as a government was in place. Much of the negotiations are expected to fall to Germany, a strong proponent of austerity. It and other euro zone countries must also consider the needs of the larger economies of Spain and Italy, which are also under intense pressure.\n" +
                "\n" +
                "Official projections showed New Democracy with 30 percent of the vote and 128 seats in the 300-seat Parliament. The Syriza party, which had surged on a wave of anti-austerity sentiment and spooked Europe with its talk of tearing up Greece’s loan agreement with its foreign creditors, was in second place, with 27 percent of the vote and 72 seats. Syriza officials had rejected calls for a coalition, ensuring its role as a vocal opposition bloc to whatever government emerges.\n" +
                "\n" +
                "But unlike in the May 6 election, when New Democracy placed first but was unable to form a government, this time intense international pressure, and the fact that the Greek government is quickly running out of money, made it likely there would be a coalition with New Democracy’s longtime rival, the socialist Pasok party. Pasok placed third in the voting, with 12 percent of the vote and 33 seats. The extreme right Golden Dawn party got 18 seats.\n" +
                "\n" +
                "Investors gave an early thumbs-up on Sunday night, pushing up the euro in value against the dollar. “It looks like we’ve avoided the worst-case scenario,” said Darren Williams, a European economist for AllianceBernstein in London. “I think that’s important, because we could have gone to a very bad place very quickly.”\n" +
                "\n" +
                "Previous rallies in response to developments in Europe were short-lived. A few weeks ago, markets initially responded positively to a bailout plan for Spanish banks, but that optimism quickly gave out when the American stock markets opened that Monday.\n" +
                "\n" +
                "The health of the world economy weighs heavily on the United States, and on President Obama’s re-election campaign. In recent days, Mr. Obama has increased the pressure on European leaders to find longer-term solutions to shoring up the euro. A White House statement on Sunday said, “As President Obama and other world leaders have said, we believe that it is in all our interests for Greece to remain in the euro area while respecting its commitment to reform.”\n" +
                "\n" +
                "In a sign of the high stakes for global financial stability, the finance ministers, the White House and the European Commission urged Greek political leaders to form a government quickly. Under the current loan agreement, the next government has just weeks to determine how to slash the equivalent of 5 percent of the country’s gross domestic product to meet budget-reduction targets.\n" +
                "\n" +
                "In a victory statement on Sunday evening, the New Democracy leader, Antonis Samaras, called for the formation of a government of national unity aimed at keeping Greece in the euro zone and renegotiating the loan agreement. “There is no time for political games. The country must be governed,” he said, adding, “We will cooperate with our European partners to boost growth and tackle the torturous problem of unemployment.”\n" +
                "\n" +
                "Alexis Tsipras, the 37-year-old leader of Syriza, conceded defeat. “We fought against blackmail to put an end to memorandum,” he said, referring to the loan agreement. “We’re proud of our fight.”\n" +
                "\n" +
                "He added that Syriza would be “present in developments from the position of the main opposition party.”\n" +
                "\n" +
                "Any new leader will face an uphill battle to inject confidence into a paralyzed Greek economy that depends heavily on the continued infusion of money from its only remaining lifeline, the European Central Bank. The Greek economy and a deficit-ridden government have lost most of their ability to raise new revenues or borrow money to continue operations.\n" +
                "\n" +
                "But political analysts said no matter what government was formed, it would be weak and likely short-lived, lacking deep popular support and the broader confidence of Europe. And it was unlikely that the election results would persuade Greece’s European lenders to extend loans without economic reforms and drastic spending cuts.\n" +
                "\n" +
                "Mr. Samaras “won a Pyrrhic victory,” said Harry Papasotiriou, a political-science professor at Panteion University in Athens. “New Democracy will try to renegotiate part of the memorandum agreement, but they won’t get far, and then they will have to implement within 100 days a very difficult program of reforms. And the unions of the public sector, supported by the radical left, will give him a hard time.”\n" +
                "\n" +
                "Asked what the election results change, Daniel Gros, the director of the Centre for European Policy Studies, which is based in Brussels, said, “Unfortunately nothing.” He said the government would most likely not be strong enough to enact the structural changes needed to turn around Greece’s uncompetitive economy.\n" +
                "\n" +
                "Meanwhile, Greece’s partners in the euro zone are growing impatient. “I think there’s no desire for them to leave, but very little inclination to say, ‘Let’s give them still another chance’ and give any substantial compromise on the rescue package,” Mr. Gros said.\n" +
                "\n" +
                "For many Greeks, the election was a choice between hope and fear. Syriza had billed itself as a kind of “Greek Spring,” capturing the momentum of those hungry for change at almost any cost from a political system that is widely seen as corrupt and ineffective. It also had support from voters who felt betrayed by the socialists, whose party was in power in 2010 when Greece signed the first of its two loan deals with foreign creditors.\n" +
                "\n" +
                "New Democracy tapped into different fears — of the unknown, of illegal immigration, of an exit from the euro zone. Its main campaign advertisement showed an elementary-school teacher telling his students which countries use the euro. When one asks, “And what about Greece?” the teacher stares back in stony silence. “Why, teacher, why?” the student asks.\n" +
                "\n" +
                "In the end, fear of imminent collapse, as opposed to the slow death of their economy and society, appeared to drive a majority of Greeks to New Democracy.\n" +
                "\n" +
                "As he watched election returns at an outdoor cafe here, Nikos Theodossiades, 69, said he was glad that New Democracy had won. “This is the only way for the country to move forward,” he said. “Staying in the euro zone, despite all its problems, is much better than the alternative.”\n" +
                "\n" +
                "Panagiotis Pierrakis, 48, an Athens taxi driver, said that he had voted for Syriza and that although it did not win, he thought the results had sent a message. The election, he said, was “a message” to Europe that “you are not the boss — Mrs. Merkel, or anybody,” a reference Chancellor Angela Merkel of Germany. “We want somebody from our country to oversee our economic system.”\n" +
                "\n" +
                "Niki Kitsantonis and Jim Yardley contributed reporting.";
        Properties p = new Properties();
        p.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(p);

//        AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
        //can also just use CRFClassifier object
        
        //Testing NER for just one article
//        printNamedEntities(pipeline, classifier, (String)myDoc.get("body"));
        printNamedEntities(pipeline, text);

    }

//    private static void printNamedEntities(StanfordCoreNLP pipeline, AbstractSequenceClassifier classifier, String text) {
    private static void printNamedEntities(StanfordCoreNLP pipeline, String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        //only because ssplit
        for (CoreMap sentence : sentences) {
//            List<List<CoreLabel>> symbols = classifier.classify(sentence.toString());
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            //not necessary, should look at pipeline annotated document
            for (CoreLabel token : tokens) {
//                for (CoreLabel label : labels) {
                    String currentLabel = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                    String currentLabel = token.get(CoreAnnotations.AnswerAnnotation.class); // should look at NamedEntityTagAnnotation
                    String currentText = token.get(CoreAnnotations.TextAnnotation.class);
                    System.out.println(currentText + "(" + currentLabel + ")");
//                }
            }
        }
    }
}

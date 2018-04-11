package org.moss.discord.fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.moss.discord.util.WordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.moss.discord.util.WordUtil.*;

public class Nonsense {
    private static final int order = 3;
    private static final String start = "B";
    private static final String end = "Z";
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(16);
    private static final Logger logger = LoggerFactory.getLogger(Nonsense.class);
    private static final Random random = new Random();

    private AtomicInteger linesProcessed;
    private AtomicInteger nodesProcessed;

    private ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> nodes = new ConcurrentHashMap<>();
    private CompletableFuture<ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>>> ready = new CompletableFuture<>();

    public Nonsense(String corpus) {
        CompletableFuture<Void>[] tasks = addToChain(corpus);
        
        CompletableFuture.allOf(tasks).thenRunAsync(() -> {
            ready.complete(nodes);
        }, executor);
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<Void>[] addToChain(String corpus) {
        List<String> lines = splitLines(corpus);
        linesProcessed = new AtomicInteger(0);
        nodesProcessed = new AtomicInteger(0);

        CompletableFuture<Void>[] tasks = (CompletableFuture<Void>[]) lines.stream()
            .map(this::addControlChars)
            .map(WordUtil::splitWords)
            .map(WordUtil::removeEmpty)
            .map(words -> CompletableFuture.runAsync(() -> parseLine(words), executor))
            .toArray(CompletableFuture[]::new);

        return tasks;
    }

    public void parseLine(List<String> words) {
        for (int i = order; i < words.size(); i++) {
            String prefix = joinWords(words.subList(i - order, i));
            String suffix = words.get(i);

            nodes.putIfAbsent(prefix, new ConcurrentHashMap<>());

            ConcurrentMap<String, AtomicInteger> node = nodes.get(prefix);
            node.putIfAbsent(suffix, new AtomicInteger(0));

            node.get(suffix).incrementAndGet();
            nodesProcessed.incrementAndGet();
        }
        int lines = linesProcessed.incrementAndGet();
        if (lines % 1000 == 0) logger.info("Parsed {} lines and {} nodes", lines, nodesProcessed.get());
    }

    private String addControlChars(String original) {
        return getInitialPrefixNode() + original + " " + end; // Add initial prefix and terminator
    }

    public String generateNonsense() {
        return predictNext(getInitialPrefixNode()).replace(getInitialPrefixNode(), "");
    }

    private String predictNext(String sentence) {
        List<String> words = removeEmpty(new ArrayList<>(splitWords(sentence)));
        String prefix = joinWords(words.subList(words.size() - order, words.size()));
        Map<String, AtomicInteger> suffixMap = nodes.get(prefix);

        if (suffixMap == null) return sentence;

        List<String> suffixes = new ArrayList<>();
        suffixMap.forEach((suffix, chance) -> {
            for (int i = 0; i < chance.get(); i++) {
                suffixes.add(suffix);
            }
        });

        String suffix = suffixes.get(random.nextInt(suffixes.size()));
        if (suffix.equals(end)) return joinWords(words);

        words.add(suffix);
        String result = joinWords(words);
        //logger.info("'{}' + '{}' -> '{}'", prefix, suffix, result);
        return predictNext(result);
    }

    public CompletableFuture<ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>>> getReady() {
        return ready;
    }

    private String getInitialPrefixNode() {
        String node = start;
        
        for (int i = 1; i < order; i++) {
            node = node + " " + start;
        }

        return node;
    }

}
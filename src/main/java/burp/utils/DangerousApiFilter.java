/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.utils;

import java.util.*;
import java.util.regex.Pattern;

public class DangerousApiFilter {
    private static final Set<String> DEFAULT_DANGEROUS_KEYWORDS = new HashSet<>(Arrays.asList(
        "delete", "del", "remove", "rm", "drop", "destroy", "kill", "terminate",
        "clear", "reset", "purge", "wipe", "erase", "clean", "flush",
        "admin", "root", "sudo", "exec", "execute", "run", "cmd", "command",
        "shutdown", "reboot", "restart", "stop", "halt", "disable",
        "format", "truncate", "empty", "void", "null", "zero"
    ));
    
    private Set<String> customDangerousKeywords;
    private boolean enabled;
    
    public DangerousApiFilter() {
        this.customDangerousKeywords = new HashSet<>(DEFAULT_DANGEROUS_KEYWORDS);
        this.enabled = true;
    }
    
    public DangerousApiFilter(Set<String> customKeywords) {
        this.customDangerousKeywords = new HashSet<>(customKeywords);
        this.enabled = true;
    }
    
    /**
     * 检查API端点是否包含危险关键词
     * @param apiEndpoint API端点URL或路径
     * @return DangerousApiResult 包含是否危险和匹配的关键词
     */
    public DangerousApiResult checkDangerousApi(String apiEndpoint) {
        if (!enabled || apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            return new DangerousApiResult(false, new ArrayList<>());
        }
        
        String lowerCaseEndpoint = apiEndpoint.toLowerCase();
        List<String> matchedKeywords = new ArrayList<>();
        
        for (String keyword : customDangerousKeywords) {
            if (lowerCaseEndpoint.contains(keyword.toLowerCase())) {
                matchedKeywords.add(keyword);
            }
        }
        
        return new DangerousApiResult(!matchedKeywords.isEmpty(), matchedKeywords);
    }
    
    /**
     * 添加自定义危险关键词
     * @param keyword 关键词
     */
    public void addDangerousKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            customDangerousKeywords.add(keyword.trim().toLowerCase());
        }
    }
    
    /**
     * 移除危险关键词
     * @param keyword 关键词
     */
    public void removeDangerousKeyword(String keyword) {
        if (keyword != null) {
            customDangerousKeywords.remove(keyword.trim().toLowerCase());
        }
    }
    
    /**
     * 设置自定义危险关键词列表
     * @param keywords 关键词集合
     */
    public void setDangerousKeywords(Set<String> keywords) {
        this.customDangerousKeywords = new HashSet<>();
        if (keywords != null) {
            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    this.customDangerousKeywords.add(keyword.trim().toLowerCase());
                }
            }
        }
    }
    
    /**
     * 获取当前危险关键词列表
     * @return 关键词集合
     */
    public Set<String> getDangerousKeywords() {
        return new HashSet<>(customDangerousKeywords);
    }
    
    /**
     * 启用或禁用过滤器
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 检查过滤器是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 重置为默认危险关键词
     */
    public void resetToDefault() {
        this.customDangerousKeywords = new HashSet<>(DEFAULT_DANGEROUS_KEYWORDS);
    }
    
    /**
     * 危险API检测结果类
     */
    public static class DangerousApiResult {
        private final boolean isDangerous;
        private final List<String> matchedKeywords;
        
        public DangerousApiResult(boolean isDangerous, List<String> matchedKeywords) {
            this.isDangerous = isDangerous;
            this.matchedKeywords = new ArrayList<>(matchedKeywords);
        }
        
        public boolean isDangerous() {
            return isDangerous;
        }
        
        public List<String> getMatchedKeywords() {
            return new ArrayList<>(matchedKeywords);
        }
        
        public String getMatchedKeywordsString() {
            return String.join(", ", matchedKeywords);
        }
    }
}
package com.example.notemaster.util;

import java.util.ArrayList;
import java.util.List;

public class NoteTemplate {
    private String name;
    private String content;

    public NoteTemplate(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() { return name; }
    public String getContent() { return content; }

    public static List<NoteTemplate> getDefaultTemplates() {
        List<NoteTemplate> templates = new ArrayList<>();

        templates.add(new NoteTemplate("空白笔记", ""));

        templates.add(new NoteTemplate("会议记录",
                "# 会议记录\n\n" +
                "**日期:** \n" +
                "**参与人:** \n" +
                "**主题:** \n\n" +
                "## 议题\n\n" +
                "1. \n\n" +
                "## 讨论内容\n\n" +
                "- \n\n" +
                "## 行动项\n\n" +
                "- [ ] \n\n" +
                "## 结论\n\n" +
                "- "));

        templates.add(new NoteTemplate("日记",
                "# " + new java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault()).format(new java.util.Date()) + "\n\n" +
                "## 今日心情\n\n" +
                "## 今日发生\n\n" +
                "- \n\n" +
                "## 今日感悟\n\n" +
                "- "));

        templates.add(new NoteTemplate("待办清单",
                "# 待办事项\n\n" +
                "## 重要且紧急\n\n" +
                "- [ ] \n\n" +
                "## 重要不紧急\n\n" +
                "- [ ] \n\n" +
                "## 紧急不重要\n\n" +
                "- [ ] \n\n" +
                "## 不重要不紧急\n\n" +
                "- [ ] "));

        templates.add(new NoteTemplate("读书笔记",
                "# 读书笔记\n\n" +
                "**书名:** \n" +
                "**作者:** \n" +
                "**阅读日期:** \n\n" +
                "## 主要内容\n\n" +
                "- \n\n" +
                "## 精彩摘录\n\n" +
                "> \n\n" +
                "## 个人感想\n\n" +
                "- "));

        templates.add(new NoteTemplate("项目计划",
                "# 项目计划\n\n" +
                "**项目名称:** \n" +
                "**截止日期:** \n\n" +
                "## 项目目标\n\n" +
                "- \n\n" +
                "## 任务分解\n\n" +
                "- [ ] 任务1\n" +
                "- [ ] 任务2\n" +
                "- [ ] 任务3\n\n" +
                "## 里程碑\n\n" +
                "1. \n\n" +
                "## 风险与应对\n\n" +
                "- "));

        templates.add(new NoteTemplate("会议议程",
                "# 会议议程\n\n" +
                "**会议主题:** \n" +
                "**时间:** \n" +
                "**地点:** \n" +
                "**参与人:** \n\n" +
                "## 议程项\n\n" +
                "1. **议题一** (预计时间: 10分钟)\n" +
                "   - \n\n" +
                "2. **议题二** (预计时间: 15分钟)\n" +
                "   - \n\n" +
                "3. **议题三** (预计时间: 10分钟)\n" +
                "   - \n\n" +
                "## 会前准备\n\n" +
                "- "));

        templates.add(new NoteTemplate("周报",
                "# 周报\n\n" +
                "**日期:** " + new java.text.SimpleDateFormat("yyyy年第w周", java.util.Locale.getDefault()).format(new java.util.Date()) + "\n\n" +
                "## 本周完成\n\n" +
                "- \n\n" +
                "## 本周问题\n\n" +
                "- \n\n" +
                "## 下周计划\n\n" +
                "- "));

        return templates;
    }
}

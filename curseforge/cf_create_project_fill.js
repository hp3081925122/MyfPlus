// Meet Your Fight Plus：CurseForge 创建项目页自动填表脚本（由 build_fill_js.py 生成）
// 用法：作者后台界面切换为英文，打开「Create A Project」页面，按 F12 打开控制台，粘贴本脚本回车
// 脚本逐步填写并自动点 Next、确认头像裁剪框；有字段未填上时停在当前步骤；不会点击最终的创建按钮
(async () => {
  const DATA = {"name": "Meet Your Fight Plus", "summary": "Enhanced versions of the four Meet Your Fight bosses with new movesets and pixel-art effects, summoned with items crafted from the original bosses' drops. Adds exclusive trophies and Curios accessories.", "license": "All Rights Reserved", "dropdowns": [["Class", "Mods"], ["Main category", "Mobs"], ["Additional categories", "Adventure and RPG"]], "distribution": "Allow distribution to 3rd party", "allowComments": true, "description": "# Meet Your Fight Plus\n\nAn addon for **Meet Your Fight** that adds enhanced versions of its four bosses. Each one has a new moveset and its own pixel-art effects. The original bosses are not changed: their summon items still summon the normal versions, and the enhanced versions use new summon items crafted from the original bosses' drops.\n\nRequires **Meet Your Fight** and **Curios API**.\n\n## Enhanced bosses\n\nEvery enhanced boss has two phases. The first lethal hit in phase 1 refills its health and plays a transition, and it only dies when phase 2 is emptied. Every damaging skill has a visible telegraph, and every random result (dice, roulette, slots) is shown to you before it takes effect.\n\n### Ancient Swampjaw\n\nA fossil predator that treats the ground like water.\n\n*   Dives into the ground and swims toward you under a wave of blocks, then bursts out and leaps at you.\n*   Fossil jaws erupt under your feet and snap shut. Spits a volley of fossil spikes that rain down on marked spots.\n*   Spinning fin blades, a double tail sweep and, in phase 2, a ring of fangs that closes on you.\n\n### Resonant Bellringer\n\nA rhythm boss: the bell sets the beat and every attack lands on a beat.\n\n*   Bell waves, echo marks that explode one beat later, and a death knell counting down above your head.\n*   Scale Quake: lines of sound pillars erupt one per beat.\n*   Belfry Resonance transition and a final grand toll in phase 2.\n\n### High-Roller Dame Fortuna\n\nThe house always shows you its hand.\n\n*   Loaded dice, roulette, a slot machine, chip fans and a Royal Flush finisher.\n*   All-In replaces the original card shuffle as her phase transition.\n\n### Rosalyne, Blade of Sunset\n\nA precise melee duelist themed around thorns and sunset.\n\n*   Three-hit dusk blade combo, thorn corridors, a parry stance that stuns careless attackers, and sunset slashes.\n*   Corolla and thorn dueling arena transitions, then the Final Waltz in phase 2.\n\n## Summon items\n\nCrafted only from the original bosses' drops, so you have to beat each original boss first (at least twice).\n\n*   Ancient Fossil Bait → Ancient Swampjaw\n*   Resonant Haunted Bell → Resonant Bellringer\n*   All-In Devil's Ante → High-Roller Dame Fortuna\n*   Sunset Dusk Key → Rosalyne, Blade of Sunset\n\n## Drops and accessories\n\nEnhanced bosses drop everything the original boss drops, plus one exclusive trophy: Ancient Jawbone, Resonant Clapper, House's Gilded Die or Sunset Rose. Each trophy crafts a Curios accessory:\n\n*   **Fossil Fang Bracelet** (bracelet): melee hits can drop a fossil spike on the target, dealing damage and Slowness II.\n*   **Resonant Belt Bell** (belt): every 4th hit releases a resonance that damages nearby enemies.\n*   **House Die Ring** (ring): rolls a die on every hit. A 1 halves the damage, 2 to 5 add a little, a 6 doubles it. Also gives +1 Luck.\n*   **Sunset Rose Cloak** (back): blocks one melee hit, counters the attacker and stuns non-boss mobs. Has a cooldown.\n\n## Config\n\n`config/myfplus-common.toml`: each boss's rework can be turned off (restoring its original skills), and you can change skill damage and cooldown multipliers, several skill parameters (beat intervals, parry window, arena radius and more) and every accessory value. `config/myfplus-client.toml` toggles screen shake, boss fog and phase banners.\n\n## License and source\n\nAll Rights Reserved. This mod does not contain Meet Your Fight's code or assets; it needs the original mod at runtime.\n\nSource: https://github.com/hp3081925122/MyfPlus\n\n---\n\n# 迎战增强\n\n**Meet Your Fight（迎战）** 的附属模组。它为本体的四个 Boss 各加了一个强化版，每个强化版都有全新的技能组和专属像素特效。本体 Boss 保持原样，本体召唤物仍然召唤普通版。强化版要用新的召唤物召唤，这些召唤物用本体 Boss 的掉落物合成。\n\n前置：**Meet Your Fight**、**Curios API**。\n\n## 强化 Boss\n\n每个强化 Boss 都有两个阶段。一阶段第一次受到致命伤时会回满血并播放转阶段演出，二阶段血量打空才会死亡。所有造成伤害的技能都有可见的前摇提示。所有随机结果（骰子、轮盘、老虎机）都会在生效前先展示给玩家。\n\n### 远古沼泽巨颌怪\n\n把地面当作水域的化石掠食者。\n\n*   钻进地里，顶着方块浪游向玩家，再破土跃起扑击。\n*   化石巨颌从玩家脚下破土咬合；吐出一簇化石骨刺，砸向事先标出的落点。\n*   回旋骨鳍刃、连续两次回身尾击；二阶段会展开獠牙环，向圆心合拢。\n\n### 万钟敲钟人\n\n节奏型 Boss：钟声定下节拍，所有攻击都在拍点上结算。\n\n*   钟波；余音印记会在一拍后爆炸；丧钟会在玩家头顶倒计时。\n*   音阶裂地：每一拍升起一排声波柱。\n*   转阶段为钟楼共振，二阶段有万钟归一大招。\n\n### 豪赌福尔图娜女爵\n\n庄家的每一手牌都摊开给你看。\n\n*   灌铅骰子、轮盘、老虎机、筹码扇射，以及皇家同花顺终结技。\n*   转阶段改为梭哈，取代本体的洗牌找牌。\n\n### 落日之锋，罗莎琳\n\n以荆棘与落日为主题的精准近战决斗者。\n\n*   暮刃三段、荆棘回廊、凋花架势（格挡反击，会眩晕无脑平 A 的玩家）、落日斩。\n*   转阶段依次是花冠和荆棘决斗场，二阶段有终幕圆舞。\n\n## 召唤物\n\n召唤物只能用本体 Boss 的掉落物合成，所以每个本体 Boss 至少要先打两次。\n\n*   远古化石之饵 → 远古沼泽巨颌怪\n*   共鸣缠魂铃铛 → 万钟敲钟人\n*   梭哈恶魔赌注 → 豪赌福尔图娜女爵\n*   落日黄昏之钥 → 落日之锋，罗莎琳\n\n## 掉落与饰品\n\n强化 Boss 会掉落本体 Boss 的全部掉落物，另外固定掉落 1 个专属物品：远古颌骨、共鸣钟舌、庄家金骰或落日玫瑰。专属物品可以合成对应的 Curios 饰品：\n\n*   **化石獠牙手镯**（手镯）：近战命中时有几率在目标头上落下化石骨刺，造成伤害并附加缓慢 II。\n*   **共鸣腰铃**（腰带）：每第 4 次命中触发共鸣，伤害周围的敌人。\n*   **庄家骰戒**（戒指）：每次造成伤害时掷骰。1 点伤害减半，2~5 点小幅加伤，6 点伤害翻倍。另外幸运 +1。\n*   **落日玫瑰披风**（背部）：格挡一次近战攻击并反击，非 Boss 生物会被眩晕。有冷却时间。\n\n## 配置\n\n`config/myfplus-common.toml`：可以单独关闭每个 Boss 的技能重做（恢复本体技能），也可以调整技能伤害倍率、冷却倍率、部分技能参数（节拍间隔、架势时长、决斗场半径等）以及所有饰品数值。`config/myfplus-client.toml` 可以开关镜头震动、Boss 雾气和阶段横幅。\n\n## 许可与源码\n\nAll Rights Reserved。本模组不包含 Meet Your Fight 的代码和资源，运行时需要安装本体。\n\n源码：https://github.com/hp3081925122/MyfPlus\n", "iconBase64": "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"};
  const report = [];

  // 判断元素是否可见
  const visible = (el) => !!el && el.getClientRects().length > 0 && getComputedStyle(el).visibility !== 'hidden';

  // 收集字段的描述文本（label、aria、placeholder、name、所在容器标题）
  const describe = (el) => {
    const parts = [el.name, el.id, el.placeholder, el.getAttribute('aria-label')];
    if (el.id) {
      const label = document.querySelector(`label[for="${CSS.escape(el.id)}"]`);
      if (label) parts.push(label.textContent);
    }
    const wrapLabel = el.closest('label');
    if (wrapLabel) parts.push(wrapLabel.textContent);
    let node = el.parentElement;
    for (let i = 0; i < 4 && node; i++, node = node.parentElement) {
      const title = node.querySelector('label, h1, h2, h3, h4, legend, .label, [class*="label"], [class*="title"]');
      if (title && !title.contains(el)) {
        parts.push(title.textContent);
        break;
      }
    }
    return parts.filter(Boolean).join(' ').toLowerCase();
  };

  // 按关键词查找字段
  const findField = (keys, excludes, selector) => {
    const list = [...document.querySelectorAll(selector)].filter(visible);
    return list.find((el) => {
      const text = describe(el);
      return keys.some((k) => text.includes(k)) && !excludes.some((k) => text.includes(k));
    });
  };

  // 以 React/Vue 可识别的方式写入输入框
  const setValue = (el, value) => {
    const proto = el.tagName === 'TEXTAREA' ? HTMLTextAreaElement.prototype : el.tagName === 'SELECT' ? HTMLSelectElement.prototype : HTMLInputElement.prototype;
    Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, value);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
    el.dispatchEvent(new Event('blur', { bubbles: true }));
  };

  const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
  const mark = (field, ok, detail) => report.push({ 字段: field, 结果: ok ? '已填写' : '未找到，需手动', 说明: detail || '' });

  // 简易 Markdown 转 HTML（用于富文本编辑器）
  const mdToHtml = (md) => {
    const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const inline = (s) => esc(s)
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
    const lines = md.split(/\r?\n/);
    const out = [];
    let i = 0;
    while (i < lines.length) {
      const line = lines[i];
      if (/^\s*$/.test(line)) { i++; continue; }
      const h = line.match(/^(#{1,6})\s+(.*)$/);
      if (h) { out.push(`<h${h[1].length}>${inline(h[2])}</h${h[1].length}>`); i++; continue; }
      if (/^---+\s*$/.test(line)) { out.push('<hr>'); i++; continue; }
      if (/^>/.test(line)) {
        const quote = [];
        while (i < lines.length && /^>/.test(lines[i])) { quote.push(lines[i].replace(/^>\s?/, '')); i++; }
        out.push('<blockquote>' + quote.filter((q) => q.trim()).map((q) => `<p>${inline(q)}</p>`).join('') + '</blockquote>');
        continue;
      }
      if (/^\|/.test(line)) {
        const rows = [];
        while (i < lines.length && /^\|/.test(lines[i])) { rows.push(lines[i]); i++; }
        const cells = (r) => r.replace(/^\||\|$/g, '').split('|').map((c) => inline(c.trim()));
        const head = cells(rows[0]);
        const body = rows.slice(2).map(cells);
        out.push('<table><thead><tr>' + head.map((c) => `<th>${c}</th>`).join('') + '</tr></thead><tbody>' +
          body.map((r) => '<tr>' + r.map((c) => `<td>${c}</td>`).join('') + '</tr>').join('') + '</tbody></table>');
        continue;
      }
      if (/^\s*[*-]\s+/.test(line)) {
        const items = [];
        while (i < lines.length && /^\s*[*-]\s+/.test(lines[i])) { items.push(lines[i].replace(/^\s*[*-]\s+/, '')); i++; }
        out.push('<ul>' + items.map((t) => `<li>${inline(t)}</li>`).join('') + '</ul>');
        continue;
      }
      const para = [];
      while (i < lines.length && !/^\s*$/.test(lines[i]) && !/^(#{1,6}\s|\||\s*[*-]\s|---|>)/.test(lines[i])) { para.push(lines[i]); i++; }
      out.push(`<p>${inline(para.join(' '))}</p>`);
    }
    return out.join('\n');
  };

  // 6. 下拉框：按标签找到下方最近的下拉触发器，点开后选择选项
  const fireClick = (el) => {
    for (const type of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
      el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }));
    }
  };
  const findLabel = (text) => [...document.querySelectorAll('label, span, div, p, h3, h4, legend')].filter(visible)
    .find((el) => norm(el.textContent) === norm(text) || norm(el.textContent) === norm(text) + ' ?');
  const findTrigger = (label) => {
    const lr = label.getBoundingClientRect();
    const candidates = [...document.querySelectorAll('select, [role="combobox"], [aria-haspopup], button, div, input')].filter(visible)
      .filter((el) => !el.contains(label) && !label.contains(el))
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.bottom - 2 && r.top - lr.bottom < 90 && r.height >= 28 && r.height <= 70 && r.width >= 120 && r.left < lr.left + 60 && r.right > lr.left)
      .sort((a, b) => (a.r.top - b.r.top) || (a.r.width * a.r.height - b.r.width * b.r.height));
    return candidates.length ? candidates[0].el : null;
  };
  const findOption = (text, exclude) => [...document.querySelectorAll('[role="option"], li, label, span, div, button')].filter(visible)
    .filter((el) => !exclude.contains(el) && el.children.length <= 3 && norm(el.textContent) === norm(text))
    .sort((a, b) => a.getBoundingClientRect().height - b.getBoundingClientRect().height)[0];
  // 统一大小写、空白和弯引号，便于匹配 Tinker's 这类选项
  const norm = (t) => (t || '').replace(/[‘’ʼ]/g, "'").replace(/[*\s]+/g, ' ').trim().toLowerCase();
  // 优先找标签下方最近的 [role="combobox"]（2026-10-26 Waystone Horizons 实测可用）
  // 先沿祖先向上找只含一个下拉框的字段容器（License 步骤标签与下拉框之间隔着一段说明文字）
  const findCombo = (label) => {
    let scope = label.parentElement;
    for (let depth = 0; scope && depth < 6; depth++, scope = scope.parentElement) {
      const combos = [...scope.querySelectorAll('[role="combobox"]')].filter(visible);
      if (combos.length === 1) return combos[0];
      if (combos.length > 1) break;
    }
    const lr = label.getBoundingClientRect();
    return [...document.querySelectorAll('[role="combobox"]')].filter(visible)
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.top && r.top - lr.bottom < 260 && r.right > lr.left - 10 && r.left < lr.left + 60)
      .sort((a, b) => a.r.top - b.r.top)[0]?.el || null;
  };
  const comboText = (el) => norm(el.value || el.innerText || el.textContent);
  const waitOption = async (text, timeout = 3000, loose = false) => {
    const end = Date.now() + timeout;
    while (Date.now() < end) {
      const option = [...document.querySelectorAll('[role="option"]')].filter(visible).find((el) => norm(el.innerText || el.textContent) === norm(text) || (loose && norm(el.innerText || el.textContent).includes(norm(text))));
      if (option) return option;
      await sleep(150);
    }
    return null;
  };
  const pickCombo = async (labelText, optionText, combo, loose = false) => {
    const container = combo.closest('[class*="select"], [class*="Select"], [class*="dropdown"], [class*="Dropdown"]') || combo.parentElement;
    if (comboText(combo) === norm(optionText) || (container?.innerText || '').split('\n').some((t) => norm(t) === norm(optionText))) {
      mark(labelText, true, `${optionText}（已选）`);
      return true;
    }
    // MUI Select 只在 mousedown（button=0）时展开，click() 不会触发；Autocomplete 输入框也响应 mousedown
    const press = (el, type, extra = {}) => el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window, button: 0, ...extra }));
    const key = (el, k) => el.dispatchEvent(new KeyboardEvent('keydown', { key: k, code: k, bubbles: true, cancelable: true }));
    combo.focus();
    press(combo, 'mousedown');
    await sleep(400);
    // Autocomplete 只输入撇号前的前缀过滤，避免直引号与弯引号不一致导致过滤为空
    const query = optionText.split(/['’]/)[0].trim();
    if (combo.tagName === 'INPUT' && !combo.readOnly) {
      setValue(combo, query);
      await sleep(500);
    }
    let option = await waitOption(optionText, 3000, loose);
    if (!option) {
      combo.focus();
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option && combo.tagName === 'INPUT') {
      setValue(combo, '');
      press(combo, 'mousedown');
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option) {
      const seen = [...document.querySelectorAll('[role="option"]')].filter(visible).map((el) => (el.innerText || el.textContent).trim()).slice(0, 12);
      mark(labelText, false, `没找到选项 ${optionText}；可见选项：${seen.join(' / ') || '无'}；控件：${combo.tagName}.${String(combo.className).split(' ').slice(0, 3).join('.')}`);
      key(document.activeElement || document.body, 'Escape');
      return true;
    }
    option.scrollIntoView({ block: 'nearest' });
    option.click();
    await sleep(600);
    // 多选菜单点完不会自动收起，用 Esc 关闭，避免挡住后面的下拉框
    const list = option.closest('[role="listbox"]');
    if (list && visible(list)) {
      key(combo, 'Escape');
      key(list, 'Escape');
      await sleep(300);
    }
    mark(labelText, true, optionText);
    return true;
  };
  const pickDropdown = async (labelText, optionText, loose = false) => {
    const label = findLabel(labelText);
    if (!label) { mark(labelText, false, '当前步骤没有该字段'); return; }
    const combo = findCombo(label);
    if (combo && await pickCombo(labelText, optionText, combo, loose)) return;
    const trigger = findTrigger(label);
    if (!trigger) { mark(labelText, false, '未找到下拉框'); return; }
    if (trigger.tagName === 'SELECT') {
      const option = [...trigger.options].find((o) => norm(o.text) === norm(optionText));
      if (option) { setValue(trigger, option.value); mark(labelText, true, optionText); } else mark(labelText, false, `没有选项 ${optionText}`);
      return;
    }
    if (norm(trigger.textContent) === norm(optionText)) { mark(labelText, true, `${optionText}（已选）`); return; }
    fireClick(trigger);
    trigger.focus && trigger.focus();
    await sleep(600);
    const option = findOption(optionText, label);
    if (option) {
      fireClick(option);
      mark(labelText, true, optionText);
      await sleep(800);
      document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
      await sleep(200);
    } else {
      mark(labelText, false, `下拉框已点开但没找到选项 ${optionText}`);
      fireClick(trigger);
    }
  };

  // 按向导步骤决定要填的字段；未知步骤全部尝试
  const STEP_FIELDS = {
    general: ['name', 'summary', 'dropdowns', 'comments', 'icon'],
    description: ['description'],
    license: ['license'],
  };
  const stepOf = () => (location.hash.match(/create\/([a-z-]+)/i) || [])[1] || 'unknown';
  const buttonByText = (re) => [...document.querySelectorAll('button')].filter(visible).find((b) => re.test(norm(b.innerText || b.textContent)) && !b.disabled);

  // 头像裁剪框：等待对话框出现后点确认按钮（图标已是正方形，默认裁剪即整张图）
  const confirmCrop = async () => {
    let dialog = null;
    for (let i = 0; i < 40 && !dialog; i++) {
      dialog = [...document.querySelectorAll('[role="dialog"], .MuiDialog-root, .MuiModal-root')].filter(visible)
        .find((el) => el.querySelector('img, canvas') && !el.closest('.MuiDrawer-root'));
      if (!dialog) await sleep(150);
    }
    if (!dialog) return '没有弹出裁剪框';
    await sleep(500);
    const buttons = [...dialog.querySelectorAll('button')].filter(visible).filter((b) => !b.disabled);
    const ok = buttons.find((b) => /^(save|apply|crop|confirm|done|ok|upload|set|use|submit)\b/.test(norm(b.innerText || b.textContent)));
    if (!ok) return `裁剪框里没找到确认按钮（按钮：${buttons.map((b) => (b.innerText || '').trim()).join(' / ') || '无'}）`;
    ok.click();
    for (let i = 0; i < 40 && dialog.isConnected && visible(dialog); i++) await sleep(150);
    return dialog.isConnected && visible(dialog) ? `已点击 ${ok.innerText.trim()}，但裁剪框没有关闭` : '';
  };

  for (let round = 0; round < 5; round++) {
    report.length = 0;
    const step = stepOf();
    const want = (field) => !STEP_FIELDS[step] || STEP_FIELDS[step].includes(field);
    let nameField = null;
    let alreadyFilled = false;
    let summaryField = null;
    let blocked = '';

    // 1. 项目名称
    if (want('name')) {
      nameField = findField(['project name', 'name'], ['slug', 'summary', 'search', 'user'], 'input[type="text"], input:not([type])');
      alreadyFilled = !!nameField && nameField.value === DATA.name;
      if (nameField) { setValue(nameField, DATA.name); mark('项目名称', true, DATA.name); } else mark('项目名称', false);
    }

    // 2. 简介
    if (want('summary')) {
      summaryField = findField(['summary', 'short description'], ['search'], 'textarea, input[type="text"], input:not([type])');
      if (summaryField) { setValue(summaryField, DATA.summary); mark('简介 Summary', true); } else mark('简介 Summary', false);
    }

    // 3. 下拉框
    if (want('dropdowns')) {
      for (const [labelText, optionText] of DATA.dropdowns) {
        await pickDropdown(labelText, optionText);
      }
    }

    // 4. 允许评论复选框
    if (want('comments') && DATA.allowComments !== false) {
      const box = [...document.querySelectorAll('input[type="checkbox"], [role="checkbox"]')]
        .find((el) => /allow comments/i.test(el.closest('label')?.innerText || el.parentElement?.innerText || el.parentElement?.parentElement?.innerText || el.getAttribute('aria-label') || ''));
      const textNode = [...document.querySelectorAll('label, span, p, div')].filter(visible).find((el) => norm(el.textContent) === 'allow comments');
      const checked = (el) => el && (el.checked === true || el.getAttribute('aria-checked') === 'true');
      if (box || textNode) {
        if (!checked(box)) {
          (box && visible(box) ? box : (box?.closest('label') || textNode)).click();
          await sleep(300);
        }
        mark('Allow Comments', !box || checked(box), box ? (checked(box) ? '已勾选' : '点击后仍未勾选，请手动勾选') : '已点击文字，请确认是否勾上');
      } else mark('Allow Comments', false, '当前步骤没有该选项');
    }

    // 5. 项目图标：写入文件输入框后自动确认裁剪框（名称已是本项目时视为已上传过）
    if (want('icon')) {
      const fileInput = [...document.querySelectorAll('input[type="file"]')].find((f) => !f.accept || /image|png/i.test(f.accept));
      if (fileInput && alreadyFilled) {
        mark('项目图标', true, '已上传过，跳过');
      } else if (fileInput) {
        const bytes = Uint8Array.from(atob(DATA.iconBase64), (c) => c.charCodeAt(0));
        const file = new File([bytes], 'cf-project-icon.png', { type: 'image/png' });
        const dt = new DataTransfer();
        dt.items.add(file);
        fileInput.files = dt.files;
        fileInput.dispatchEvent(new Event('input', { bubbles: true }));
        fileInput.dispatchEvent(new Event('change', { bubbles: true }));
        const cropError = await confirmCrop();
        if (cropError && cropError !== '没有弹出裁剪框') {
          mark('项目图标', false, cropError);
          blocked = '请手动处理头像裁剪框后再运行脚本';
        } else mark('项目图标', true, cropError || '已上传并确认裁剪');
      } else mark('项目图标', false, '请手动上传 curseforge/icon.png');
    }

    // 6. 描述：先切到 Markdown（若页面提供），优先 Markdown 文本框，否则写入富文本编辑器
    if (want('description')) {
      await sleep(800);
      let markdownMode = false;
      const formatSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => /markdown/i.test(o.text)));
      if (formatSelect) {
        const option = [...formatSelect.options].find((o) => /markdown/i.test(o.text));
        setValue(formatSelect, option.value);
        markdownMode = true;
        await sleep(500);
      } else {
        const toggle = [...document.querySelectorAll('button, label, [role="tab"], [role="radio"], input[type="radio"]')].filter(visible)
          .find((el) => /^\s*markdown\s*$/i.test(el.textContent || el.value || ''));
        if (toggle) { toggle.click(); markdownMode = true; await sleep(500); }
      }
      const descField = findField(['description'], ['summary', 'search'], 'textarea');
      const plainTextareas = [...document.querySelectorAll('textarea')].filter(visible).filter((t) => t !== summaryField);
      const targetTextarea = descField || (markdownMode || step === 'description' ? plainTextareas[0] : null);
      if (targetTextarea) {
        setValue(targetTextarea, DATA.description);
        mark('描述', true, 'Markdown 文本框');
      } else {
        let editor = [...document.querySelectorAll('[contenteditable="true"], .ProseMirror, .ql-editor')].filter(visible)[0];
        if (!editor) {
          const frame = [...document.querySelectorAll('iframe')].find((f) => { try { return f.contentDocument && f.contentDocument.body && f.contentDocument.body.isContentEditable; } catch (e) { return false; } });
          if (frame) editor = frame.contentDocument.body;
        }
        if (editor) {
          editor.focus();
          const doc = editor.ownerDocument;
          const sel = doc.getSelection();
          const range = doc.createRange();
          range.selectNodeContents(editor);
          sel.removeAllRanges();
          sel.addRange(range);
          const html = mdToHtml(DATA.description);
          if (!doc.execCommand('insertHTML', false, html)) editor.innerHTML = html;
          editor.dispatchEvent(new Event('input', { bubbles: true }));
          mark('描述', true, '富文本编辑器（请检查表格排版）');
        } else mark('描述', false, '没找到描述编辑器，请手动粘贴 description.md');
      }
    }

    // 7. 许可证：原生下拉框或 MUI 下拉框，选项文本包含目标即可（如 MIT License）
    if (want('license') && DATA.license) {
      const licenseText = DATA.license.toLowerCase();
      const licenseSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => o.text.toLowerCase().includes(licenseText)));
      if (licenseSelect) {
        const option = [...licenseSelect.options].find((o) => o.text.toLowerCase().includes(licenseText));
        setValue(licenseSelect, option.value);
        mark('许可证', true, option.text);
      } else {
        await pickDropdown(findLabel('Project License') ? 'Project License' : 'License', DATA.license, true);
      }
    }

    // 8. 第三方分发：默认允许
    if (want('license') && DATA.distribution) {
      await pickDropdown('Project distribution', DATA.distribution);
    }

    console.log(`%c步骤 ${step}`, 'font-weight:bold');
    console.table(report);
    const failed = report.filter((r) => r.结果 !== '已填写');
    if (blocked || failed.length) {
      console.log(`%c${blocked || '有字段未填上，已停在当前步骤；处理后可再次运行脚本'}`, 'color:#c92a2a;font-weight:bold');
      break;
    }
    // 9. 只点 Next，不点创建按钮；进入下一步后继续填写
    const next = buttonByText(/^next$/);
    if (!next) {
      console.log('%c已到最后一步：请核对全部内容，确认无误后手动点击创建按钮。', 'color:#2b8a3e;font-weight:bold');
      break;
    }
    const before = location.hash;
    await sleep(400);
    next.click();
    let moved = false;
    for (let i = 0; i < 60 && !moved; i++) { await sleep(150); moved = location.hash !== before; }
    if (!moved) {
      console.log('%c点击 Next 后页面没有前进，可能有必填项未通过校验，请查看页面提示。', 'color:#c92a2a;font-weight:bold');
      break;
    }
    await sleep(1200);
  }
})();

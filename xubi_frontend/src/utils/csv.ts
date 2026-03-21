/**
 * CSV 解析工具
 * 提供：parseCsvRobust, csvToTable
 */
export const parseCsvRobust = (csvText: string): string[][] => {
  if (!csvText) return [];
  // 去 BOM
  let csv = csvText.replace(/^\uFEFF/, '');
  // 规范换行
  csv = csv.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  const parseWithDelimiter = (text: string, delim: string): string[][] => {
    const rows: string[][] = [];
    let cur = '';
    let inQuotes = false;
    let row: string[] = [];
    for (let i = 0; i < text.length; i++) {
      const ch = text[i];
      if (ch === '"') {
        if (inQuotes && text[i + 1] === '"') {
          cur += '"';
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (ch === delim && !inQuotes) {
        row.push(cur);
        cur = '';
      } else if (ch === '\n' && !inQuotes) {
        row.push(cur);
        rows.push(row);
        row = [];
        cur = '';
      } else {
        cur += ch;
      }
    }
    if (cur !== '' || row.length > 0) {
      row.push(cur);
      rows.push(row);
    }
    while (rows.length > 0 && rows[rows.length - 1].every((c) => c === '')) rows.pop();
    return rows;
  };

  const candidates = [',', '\t', ';', '|'];
  let best: { delim: string; rows: string[][]; maxCols: number } | null = null;
  for (const d of candidates) {
    const r = parseWithDelimiter(csv, d);
    let maxCols = 0;
    for (let i = 0; i < Math.min(20, r.length); i++) {
      const nn = r[i].filter((c) => c !== '').length;
      if (nn > maxCols) maxCols = nn;
    }
    if (!best || maxCols > best.maxCols) {
      best = { delim: d, rows: r, maxCols };
    }
  }
  const rows = best ? best.rows : [];

  if (rows.length > 0) {
    const maxColsOverall = rows.reduce((m, r) => Math.max(m, r.filter((c) => c !== '').length), 0);
    if (maxColsOverall <= 1) {
      return rows;
    }
  }
  return rows;
};

export const csvToTable = (csv: string) => {
  const rows = parseCsvRobust(csv || '');
  if (!rows || rows.length === 0) {
    return { columns: [], dataSource: [], summary: [] };
  }

  let headerIndex = 0;
  let maxNonEmpty = 0;
  for (let i = 0; i < rows.length; i++) {
    const count = rows[i].filter((c) => (c ?? '').toString().trim() !== '').length;
    if (count > maxNonEmpty) {
      maxNonEmpty = count;
      headerIndex = i;
    }
  }

  if (maxNonEmpty <= 1) {
    return { columns: [], dataSource: [] };
  }

  const headers = rows[headerIndex].map((h) => (h ?? '').toString().trim());
  const columns = headers.map((h, i) => ({
    title: h || `列${i + 1}`,
    dataIndex: `col_${i}`,
    key: `col_${i}`,
    ellipsis: true,
    // 默认宽度：序号/姓名更宽，其它列保持统一宽度，便于横向滚动与列伸缩
    width: i === 0 ? 100 : i === 1 ? 140 : 110,
  }));

  // 分离可能的汇总行（例如：班级平均分/最高分/最低分）
  const allDataRows = rows.slice(headerIndex + 1).filter((r) => r.some((c) => (c ?? '').toString().trim() !== ''));

  const summaryLabels = ['班级平均分', '最高分', '最低分', '平均分'];
  const summary: any[] = [];
  const dataRows: string[][] = [];
  // helper 判断是否是数字（整数或小数）
  const isNumeric = (s: any) => {
    if (s === null || s === undefined) return false;
    const t = String(s).trim();
    if (t === '') return false;
    return !Number.isNaN(Number(t));
  };

  for (const r of allDataRows) {
    const first = (r[0] ?? '').toString().trim();
    if (summaryLabels.some((lab) => first.indexOf(lab) !== -1)) {
      // 构造 summary 行：将 label 放在序号列 (col_0)，姓名(col_1) 与班级排名(col_last) 置空，数值按学科列依次填入
      const item: any = { key: `summary_${summary.length}`, _label: first };
      // 收集行中所有数值型单元格（跳过首个作为 label）
      const numericTokens: string[] = [];
      for (let i = 1; i < r.length; i++) {
        if (isNumeric(r[i])) numericTokens.push((r[i] ?? '').toString().trim());
      }

      // 填充列：col_0 = label
      for (let i = 0; i < headers.length; i++) {
        if (i === 0) {
          item[`col_${i}`] = first;
        } else if (i === 1) {
          // 姓名列保持空
          item[`col_${i}`] = '';
        } else if (i === headers.length - 1) {
          // 班级排名列保持空
          item[`col_${i}`] = '';
        } else {
          // 其它列按顺序填数值
          item[`col_${i}`] = numericTokens.length > 0 ? numericTokens.shift() : '';
        }
      }
      summary.push(item);
    } else {
      dataRows.push(r);
    }
  }

  const dataSource = dataRows.map((r, ri) => {
    const item: any = { key: ri };
    for (let i = 0; i < headers.length; i++) {
      item[`col_${i}`] = r[i] ?? '';
    }
    return item;
  });

  return { columns, dataSource, summary };
};

export default { parseCsvRobust, csvToTable };

// Formats a number as Indian rupees, e.g. 1400 -> ₹1,400.00
export const money = (n) =>
  '₹' +
  Number(n || 0).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
